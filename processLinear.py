# -*- coding:utf-8 -*-

import os
import pickle
from sklearn import linear_model
from sklearn.model_selection import train_test_split
import numpy as np
import re


class processLinear():

    def __init__(self, fname, limit):
        self.name = fname
        self.loadFile(fname, limit)
        self.newdata = []

    def loadFile(self, fname, limit):
        self.data = np.loadtxt(fname, delimiter=',', usecols=(0, 1, 2, 3))
        # self.data = self.trimData(data, 10)
        self.limit = limit
        self.size = len(self.data)

    def getData(self):
        self.getStep(self.limit)
        self.getDistance(self.name)
        self.getSpeed()
        # print(self.name, self.distance, self.stepSpeed, self.speed)
        return [self.stepSpeed, self.speed]

    def trimData(self, data, step=10):
        need = list(range(0, len(data), step))
        return data[need]

    def getDistance(self, fname):
        flag = int(re.findall(r'1582626951637_直行_(\d+)_Linear.txt', fname)[0])
        if flag < 6:
            self.distance = 30
        else:
            self.distance = 60

    def getSpeed(self):
        '''
        speed: m/s
        '''
        self.speed = self.distance * 1000 / (self.data[-1,0] - self.data[0,0])
        self.stepSpeed = self.step * 1000 / (self.data[-1,0] - self.data[0,0])

    def getStep(self, limit):
        def f1(line):
            return (line[1]**2 + line[2]**2 + line[3]**2)**0.5

        vArr = [f1(line) for line in self.data]

        newVArr = []
        newVArr.append(vArr[0])
        last = vArr[1]
        up = True
        lastStep = vArr[1] > vArr[0]
        for v in vArr[2:]:
            up = v > last
            if up != lastStep:
                newVArr.append(last)
            last = v
            lastStep = up

        last = newVArr[0]
        count = 0
        for v in newVArr:
            if abs(v - last) < 5:
                count += 1
            last = v
        self.step = int((len(newVArr) - count) / 2)

    def divideData(self, step):
        return self.divideLinearData(step)

    def divideLinearData(self, step):
        '''
        使用加速度全部三个轴的数据
        扩充数据量
        1. 交替分组 1-5 2-6 可增大size的倍数
        '''
        for last in range(step):
            for index in range(step + last, self.size + 1, step):
                line = list(self.data[last:index, 3].flatten())
                last = index
                line.append(self.speed)
                self.newdata.append(line)
        return self.newdata


class RegrModel():
    def __init__(self, datafile):
        with open(datafile, 'rb') as f:
            data = pickle.load(f)
        self.data = np.array(data)
        # np.random.shuffle(self.data)
        self.X = self.data[:, :-1]
        self.Y = self.data[:, -1]
        # x为数据集的feature熟悉，y为label
        # self.x_train, self.x_test, self.y_train, self.y_test = train_test_split(self.X, self.Y, test_size = 0.3)
        # print('shage ', np.shape(self.x_train), np.shape(self.y_train), np.shape(self.x_test), np.shape(self.y_test))

    def fit(self, Model):
        regr = Model
        regr.fit(self.X, self.Y)   # 注意此处.reshape(-1, 1)，因为X是一维的！
        # regr.fit(self.x_train, self.y_train)   # 注意此处.reshape(-1, 1)，因为X是一维的！
        self.model = regr
        print('准确率:', regr.score(self.X, self.Y))
        self.coef = list(regr.coef_)
        self.intercept = regr.intercept_
        print(self.coef)
        print(self.intercept)
        # self.predict(self.x_test[22])
        return regr.score(self.X, self.Y)
        # print('yes:=> ', self.y_test[22])

    def predict(self, one):
        res = 0
        for i, v in enumerate(one):
            res += v * self.coef[i]
        res += self.intercept
        # print("1===> ", res)
        # print(self.model.predict([one]))


def divide(folderList, whichFile, limit, testSize=-1):
    D = []
    count = 0
    for folder in folderList:
        for fname in os.listdir(folder):
            if whichFile in fname:
                filepath = os.path.join(folder, fname)
                process = processLinear(filepath, limit)
                D.append(process.getData())
                count += 1
            if count == testSize:
                break
        if count == testSize:
            break
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    D.append([0, 0])
    with open('divide.dat', 'wb') as f:
        pickle.dump(D, f)


if __name__ == "__main__":
    '''
    groupSize|score
    5|.42
    '''

    for groupSize in range(2, 20):
        divide(['godata'], 'Linear', groupSize)
        m = RegrModel('divide.dat')
        sum = m.fit(linear_model.LinearRegression())
        print('groupSize:%2d => 准确率:%.2f' % (groupSize, sum))
    # m = RegrModel('divide.dat')
    # m.fit(linear_model.LinearRegression())
