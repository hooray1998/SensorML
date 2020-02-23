# -*- coding:utf-8 -*-

from sklearn import linear_model
import numpy as np


def readAllData(fname):

    data = np.loadtxt(fname, delimiter=',', usecols=(0, 1, 2))
    X = np.loadtxt(fname, delimiter=',', usecols=(3))
    Y = np.loadtxt(fname, delimiter=',', usecols=(4))
    return data, X, Y


D, X, Y = readAllData('左转.txt')

# xx, yy = np.meshgrid(np.linspace(0,10,10), np.linspace(0,100,10))
# zz = 1.0 * xx + 3.5 * yy + np.random.randint(0,100,(10,10))
# 构建成特征、值的形式
# D, X = np.column_stack((xx.flatten(),yy.flatten())), zz.flatten()

regr = linear_model.LinearRegression()

# 拟合
regr.fit(D, X)   # 注意此处.reshape(-1, 1)，因为X是一维的！

one = [0.025267962,-0.1444133,-0.045039434,0.34926027,0.348195,0.45761275,0.4950492,0.58133554,0.62364167,0.8831095]
# -0.8745756130765759,-0.48488915951146133]

# XX = regr.predict(D)

print(regr.score(D, X))
# for i in range(len(X)):
    # print(X[i], ' => ')
    # print(XX[i], '\n')
    # a = input(" ")
