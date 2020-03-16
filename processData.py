# -*- coding:utf-8 -*-

import os
import pickle
from sklearn import linear_model
from sklearn.model_selection import train_test_split
import numpy as np


class processData():

    def __init__(self, fname):
        self.name = fname
        self.loadFile(fname)
        self.newdata = []

    def loadFile(self, fname):
        self.data = np.loadtxt(fname, delimiter=',', usecols=(1, 2, 3))
        self.size = len(self.data)
        self.getDegrees(fname)
        self.getPerDegrees()

    def getDegrees(self, fname):

        if '直行' in fname:
            self.degrees = 0
        elif '左转' in fname:
            self.degrees = 90
        elif '右转' in fname:
            self.degrees = -90
        elif '左掉头' in fname:
            self.degrees = 180
        elif '右掉头' in fname:
            self.degrees = -180
        # print('degrees:', self.degress, end=',')

    def getPerDegrees(self):
        self.preDegress = self.degrees / self.size
        # print('pre:', self.preDegress)

    def divideData(self, step):
        if 'Angular' in self.name:
            return self.divideAngularDataPro(step)
        elif 'Orient' in self.name:
            return self.divideOrientData(step)

    def divideAngularData(self, step):
        '''
        使用陀螺仪全部三个轴的数据
        '''
        changeDegress = step * self.preDegress
        last = 0
        # print(np.mean(self.data, axis=0)) # 查看每个轴的平均数
        # return
        for index in range(step, self.size, step):
            line = list(self.data[last:index, :].flatten())
            line.append(changeDegress)
            last = index
            self.newdata.append(line)
        return self.newdata

    def divideAngularDataPro(self, step):
        '''
        使用陀螺仪全部三个轴的数据
        扩充数据量
        1. 交替分组 1-5 2-6 可增大size的倍数
        '''
        changeDegress = step * self.preDegress
        for last in range(step):
            for index in range(step + last, self.size + 1, step):
                line = list(self.data[last:index, :].flatten())
                last = index
                line.append(changeDegress)
                self.newdata.append(line)
        return self.newdata

    def divideOrientData(self, step):
        '''
        使用方向传感器全部三个轴的数据
        轴的数据是连续的，感觉不能再处理了
        '''
        changeDegress = step * self.preDegress
        last = 0
        # print(np.mean(self.data, axis=0))
        # return
        for index in range(step, self.size - 1, step):
            x, y, z = list(self.data[index, :] - self.data[last, :])
            line = [self.normalize(x), self.normalize(
                y), self.normalize(z), changeDegress]
            last = index
            self.newdata.append(line)
            # print(line)
        return self.newdata

    def normalize(self, changeDegress):
        '''
        将度数变化统一到-180~180之间
        '''
        if changeDegress > 180:
            return changeDegress - 360
        elif changeDegress < -180:
            return changeDegress + 360
        else:
            return changeDegress


class RegrModel():
    def __init__(self, datafile):
        with open(datafile, 'rb') as f:
            data = pickle.load(f)
        self.data = np.array(data)
        self.X = self.data[:, :-1]
        self.Y = self.data[:, -1]
        # x为数据集的feature熟悉，y为label
        self.x_train, self.x_test, self.y_train, self.y_test = train_test_split(self.X, self.Y, test_size = 0.3)
        # print('shape ', np.shape(self.x_train), np.shape(self.y_train), np.shape(self.x_test), np.shape(self.y_test))

    def fit(self, Model):
        regr = Model
        regr.fit(self.X, self.Y)   # 注意此处.reshape(-1, 1)，因为X是一维的！
        # regr.fit(self.x_train, self.y_train)   # 注意此处.reshape(-1, 1)，因为X是一维的！
        self.model = regr
        print('准确率:', regr.score(self.x_test, self.y_test))
        self.coef = list(regr.coef_)
        self.intercept = regr.intercept_
        print(self.coef)
        print(self.intercept)
        # self.predict(self.x_test[22])
        # return regr.score(self.x_test, self.y_test)
        # print('yes:=> ', self.y_test[22])

    def predict(self, one):
        res = 0
        for i, v in enumerate(one):
            res += v * self.coef[i]
        res += self.intercept
        # print("1===> ", res)
        # print(self.model.predict([one]))


def divide(folderList, whichFile, groupSize):
    D = []
    for folder in folderList:
        for fname in os.listdir(folder):
            if whichFile in fname:
                filepath = os.path.join(folder, fname)
                process = processData(filepath)
                D.extend(process.divideData(groupSize))
    with open('divide.dat', 'wb') as f:
        pickle.dump(D, f)


if __name__ == "__main__":
    '''
    groupSize|score
    5|.42
    '''

    # time = 50
    # for groupSize in range(1, 20):
    # sum = 0
    # for i in range(time):
    # divide(['newalldata/', 'alldata/'], 'Angular', groupSize)
    # m = RegrModel('divide.dat')
    # sum += m.fit(linear_model.LinearRegression())
    # print('groupSize:%2d => 准确率:%.2f' % (groupSize, sum / time))
    divide(['newalldata/', 'alldata/'], 'Angular', 10)
    m = RegrModel('divide.dat')
    m.fit(linear_model.LinearRegression())
