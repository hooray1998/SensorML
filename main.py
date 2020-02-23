# -*- coding:utf-8 -*-

import os
import numpy as np

D = {}


class unionUData():
    '''
    d1: t, x, y
    d2: t, 陀螺仪
    '''

    def __init__(self, fname):
        self.name = fname
        self.loadFile(fname)
        self.curIndex = 1
        self.polyfit()
        self.data = []
        self.speedChangeData = []

    def loadFile(self, fname):
        self.d1 = np.loadtxt(fname, delimiter=',', usecols=(0, 1, 2))
        self.d2 = np.loadtxt(fname.replace('Gps', 'Angular'), delimiter=',',
                usecols=(0, 3))

    def getPosition(self, time):
        x = np.polyval(self.p1, time)
        y = np.polyval(self.p2, time)
        return x, y

    def polyfit(self):
        self.p1 = np.polyfit(self.d1[:, 0], self.d1[:, 1], 2)
        print('======>', np.poly1d(self.p1))
        self.p2 = np.polyfit(self.d1[:, 0], self.d1[:, 2], 2)
        print('======>', np.poly1d(self.p2))

    def union(self):
        for line in self.d2:
            x, y = self.getPosition(line[0])
            self.data.append([line[0], line[1], x, y])
        print('len:%d => %d' % (len(self.d2), len(self.data)))
        for l in self.data:
            print(l)

    def speed(self):
        '''
        每十条传感器数据产生一个速度,以及速度变化
        '''
        step = 3
        y2x = 94 / 111
        speedData = []
        for index in range(0, len(self.data), step):
            if index + step < len(self.data):
                move_x = self.data[index + step][2] - self.data[index][2]
                move_y = (self.data[index + step][3] - self.data[index][3]) * \
                    y2x
                curStep = []
                for index2 in range(index, index+step):
                    curStep.append(self.data[index2][1])
                curStep.append(move_x)
                curStep.append(move_y)
                speedData.append(curStep)
        print('speed', len(speedData))
        print('================')
        # print(speedData)

        lastx, lasty = speedData[0][-2:]
        for line in speedData[1:]:
            change_x = line[-2] - lastx
            change_y = line[-1] - lasty
            norm_x, norm_y = self.normlize(change_x, change_y)
            line[-2:] = [norm_x, norm_y]
            self.speedChangeData.append(line)
        return self.speedChangeData

    def normlize(self, x, y):
        xxyy = (x**2 + y**2)**0.5
        if not xxyy:
            return 0, 0
        norm_x = x / xxyy
        norm_y = y / xxyy
        return norm_x, norm_y

    def save(self, fname):
        fname = fname.replace('Gps', 'Union')
        with open(fname, 'w') as f:
            for line in self.data:
                f.write(','.join(map(lambda x: str(x), line))+'\n')


allfile = [os.path.join('files', f) for f in os.listdir('files/') if 'Gps' in f]
print(allfile)

# for f in [allfile[3]]:
D = []
for fname in allfile:

    if '左转' in fname:
        un = unionUData(fname)
        un.union()
        # un.speed()
        D.extend(un.speed())
        # un.save(f)
        # break

# print(D)
np.savetxt('左转.txt', np.array(D), delimiter=',')
'''
with open('all3.txt', 'w') as f:
    for line in D:
        f.write(','.join(map(lambda x: str(x), line)) + '\n')
'''
print('all:', len(D))
