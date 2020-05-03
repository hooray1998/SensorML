# -*- coding:utf-8 -*-


import numpy as np


t = np.loadtxt('./newfiles/Gps_右转1581225557270ms.txt', delimiter=',', usecols=(0, 1, 2))
print(t[:,0])

# z1 = np.polyfit(x, y, 0.5)#用3次多项式拟合
# print(np.polyval(z1, 4))
# p1 = np.poly1d(z1)
# print(p1) #在屏幕上打印拟合多项式

