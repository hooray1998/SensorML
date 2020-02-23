# -*- coding:utf-8 -*-
'''
扩充数据量的方法
1. 交替分组 1-5 2-6 可增大size的倍数
'''

import os
import numpy as np
from sklearn import linear_model
from sklearn.model_selection import train_test_split


class processData():
    '''
    '''

    def __init__(self, fname):
        self.name = fname
        self.loadFile(fname)
        self.newdata = []

    def loadFile(self, fname):
        self.data = np.loadtxt(fname, delimiter=',', usecols=(1, 2, 3))
        self.size = len(self.data)
        self.getDegress(fname)
        self.getPerDegress()

    def getDegress(self, fname):
        if '直行' in fname:
            self.degress = 0
        elif '左转' in fname:
            self.degress = 90
        elif '右转' in fname:
            self.degress = -90
        elif '左掉头' in fname:
            self.degress = 180
        elif '右掉头' in fname:
            self.degress = -180
        # print('degress:', self.degress, end=',')

    def getPerDegress(self):
        self.preDegress = self.degress / self.size
        # print('pre:', self.preDegress)

    def divideData(self, step):
        if 'Angular' in self.name:
            return self.divideAngularData(step)
        elif 'Orient' in self.name:
            return self.divideOrientData(step)

    def divideAngularData(self, step):
        '''
        使用陀螺仪全部三个轴的数据
        '''
        changeDegress = step * self.preDegress
        last = 0
        # print(np.mean(self.data, axis=0))
        # return
        for index in range(step, self.size, step):
            line = list(self.data[last:index, :].flatten())
            line.append(changeDegress)
            last = index
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
            line = [self.normalize(x), self.normalize(y), self.normalize(z), changeDegress]
            last = index
            self.newdata.append(line)
            print(line)
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
    def __init__(self, data):
        self.data = np.array(data)
        self.X = self.data[:, :-1]
        self.Y = self.data[:, -1]
        # x为数据集的feature熟悉，y为label
        self.x_train, self.x_test, self.y_train, self.y_test = train_test_split(self.X, self.Y, test_size = 0.3)
        print('shage ', np.shape(self.x_train), np.shape(self.y_train), np.shape(self.x_test), np.shape(self.y_test))

    def fit(self, Model):
        regr = Model
        regr.fit(self.x_train, self.y_train)   # 注意此处.reshape(-1, 1)，因为X是一维的！
        print(regr.score(self.x_test, self.y_test))


if __name__ == "__main__":
    D = []
    folder = 'alldata'
    whichFile = 'Angular'
    groupSize = 20
    for fname in os.listdir(folder):
        if whichFile in fname:
            filepath = os.path.join(folder, fname)
            process = processData(filepath)
            D.extend(process.divideData(groupSize))

    m = RegrModel(D)
    m.fit(linear_model.LinearRegression())
