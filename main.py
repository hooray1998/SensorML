# -*- coding:utf-8 -*-
import numpy as np
import os
import matplotlib.pyplot as plt
from calStep import calStep
from calDegree import averageOrient, recognizeStraight

'''
数据采集
要很清楚的知道你当前这一段时间走了多少步
人眼数的也不一定对
然后加速或者减速

要求：
识别变速过程中准确的步数

思路：
首先设置一个步数频率间隔T
和一个幅值阈值V
然后根据这些阈值识别出来五步
    计算这五步的平均值avg，和中位数mid，最小值min
    根据avgx0.6确定新的V
    根据这些步数的频率计算平均值avg2，更新T=avg2 * 0.8
    然后再识别出新的五步
'''


def mo(l):
    return sum([v**2 for v in l])**0.5


def loadData(folderList, curProcess):
    dataMat = []
    for folder in folderList:
        for fname in os.listdir(folder):
            if curProcess in fname:
                print(fname)
                data = np.loadtxt(os.path.join(folder, fname), delimiter=',')
                time = data[:, 0]
                time = time - time[0]
                if 'Ori' in fname:
                    values = np.array([l for l in data[:, 1]])
                elif 'Lin' in fname:
                    values = np.array([mo(l) for l in data[:, 1:]])
                dataMat.append([time, values])
    return dataMat


def main():
    process = 'Lin'
    dataMat = loadData(['./419-data/'], process)

    for times, values in dataMat:
        plt.plot(times, values)
        if 'O' in process:
            newT, newV = averageOrient(times, values, 1000)
            plt.plot(newT, newV, 'o-', ms=8)
            newT, newV, goLen, goOrient = recognizeStraight(newT, newV)
            plt.title("文件名 => %s" % (process))
            plt.plot(newT, newV, 'v', ms=20)
            for i in range(0, len(goLen)):
                print(newT[i], goLen[i] * 2, goOrient[i])
        elif 'L' in process:
            topList = calStep(times, values)
            plt.title("步数 => %d" % (len(topList)))
            plt.plot(times[topList], values[topList], 'o')
        mng = plt.get_current_fig_manager()
        mng.full_screen_toggle()
        plt.show()


if __name__ == "__main__":
    main()
