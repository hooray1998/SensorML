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

def getData(filepath):
    data = np.loadtxt(filepath, delimiter=',')
    time = data[:, 0]
    time = time - time[0]
    if 'Ori' in filepath:
        values = np.array([l for l in data[:, 1]])
    elif 'Lin' in filepath:
        values = np.array([mo(l) for l in data[:, 1:]])
    return [time, values]


def loadData(folderList, curProcess):
    dataMat = []
    for folder in folderList:
        if '.txt' in folder:
            fname = folder
            if curProcess in fname:
                print(fname)
                dataMat.append(getData(fname))
        else:
            for fname in os.listdir(folder):
                if curProcess in fname:
                    print(fname)
                    dataMat.append(getData(os.path.join(folder, fname)))
    return dataMat


def main():
    process = 'Ori'
    dataMat = loadData(['./niceData/'], process)

    for times, values in dataMat:
        plt.plot(times, values)
        if 'O' in process:
            newT, newV = averageOrient(times, values, 1000)
            plt.plot(newT, newV, 'o-', ms=3)
            newT, newV, goLen, goOrient = recognizeStraight(newT, newV)
            plt.title("Orient")
            #  plt.plot(newT, newV, 'v', ms=20)
            for i in range(0, len(goLen)):
                print(newT[i], goLen[i] * 2, goOrient[i])
        elif 'L' in process:
            topList = calStep(times, values)
            plt.title("Count => %d" % (len(topList)))
            plt.plot(times[topList], values[topList], 'o', ms=3)

        timeArr = [[  0, 12 ],
                   [ 13, 51 ],
                   [ 52,100 ],
                   [101,193 ],
                   [194,322 ],
                   [323,395 ],
                   [396,401 ],
                   [402,437 ],
                   [438,480 ],
                   [481,487 ],
                   [490,498 ],
                   [499,510 ],
                   [511,529 ],
                   [533,600 ],
                   [601,657 ],
                   [658,925 ],
                   [926,1067]]

        for i,t in enumerate(timeArr):
            c = ['g','k'][i%2]
            plt.hlines(-100, t[0]*1000, t[1]*1000, colors=c, linestyles='solid', lw=20)
            plt.vlines(t[0]*1000, -100, 400, colors='r', linestyles='dashed', lw=1)
            plt.vlines(t[1]*1000, -100, 400, colors='r', linestyles='dashed', lw=1)
        plt.xticks(fontsize=20)
        plt.yticks(fontsize=20)
        plt.xlabel('time/ms', fontsize=20)
        plt.ylabel('orient/degree', fontsize=20)
        plt.title('Orientation', fontsize=20)


#    0~ 12s |  10m |  63.6° |  12 | 0.8 m/s
#   13~ 51s |   8m |  84.3° |  38 | 0.2 m/s
#   52~100s | 108m |  68.2° |  48 | 2.3 m/s
#  101~193s | 204m |  69.0° |  92 | 2.2 m/s
#  194~322s | 268m | 329.1° | 128 | 2.1 m/s
#  323~395s | 170m |  64.5° |  72 | 2.4 m/s
#  396~401s |  10m | 147.0° |   5 | 2.0 m/s
#  402~437s |  84m | 169.1° |  35 | 2.4 m/s
#  438~480s |  92m | 175.5° |  42 | 2.2 m/s
#  481~487s |   8m | 142.7° |   6 | 1.3 m/s
#  490~498s |   2m | 133.5° |   8 | 0.3 m/s
#  499~510s |   6m | 178.7° |  11 | 0.5 m/s
#  511~529s |   4m | 219.2° |  18 | 0.2 m/s
#  533~600s |  96m |  61.2° |  67 | 1.4 m/s
#  601~657s |  40m | 310.4° |  56 | 0.7 m/s
#  658~925s | 580m | 335.4° | 267 | 2.2 m/s
#  926~1067s | 300m |  78.9° | 141 | 2.1 m/s
        mng = plt.get_current_fig_manager()
        mng.full_screen_toggle()
        plt.show()


if __name__ == "__main__":
    main()
