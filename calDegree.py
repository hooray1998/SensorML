# -*- coding:utf-8 -*-
'''
根据连续的五秒判断当前五秒是不是在直行
如果是拐弯不处理
如果是直行记录
两次直行相差的度数作为拐弯的度数

判断直行的方法：
    1. 起点终点相邻两点的均值差小于15
'''


def transform(standard, needTransform):
    """
    将need的角度变换到和standard的距离在180以内

    :param standard float: 标准方向
    :param needTransform float: 需要变换的方向
    """
    if abs(standard - needTransform) <= 180:
        return needTransform
    elif needTransform > standard:
        return needTransform - 360
    else:
        return needTransform + 360


def diffDegress(a, b):
    """
    返回b相对a变换的角度，范围 (-180, 180)

    :param a float: 第一个方向
    :param b float: 第二个方向
    """
    diff = a - b
    while diff < -180:
        diff += 360
    while diff > 180:
        diff -= 360
    return diff


def calGoOrientAvg(dList):
    newList = [transform(dList[0], curDegree) for curDegree in dList]
    return sum(newList) / len(dList)


def judgeStraight(dList, limit=15, score=0.8):
    avg = calGoOrientAvg(dList)
    # 80% 满足即可
    count = 0
    for degree in dList:
        if abs(diffDegress(degree, avg)) <= limit:
            count += 1
    return count / len(dList) >= score


def averageOrient(times, values, timeLength=1000):
    """
    每两秒求个平均值

    :param times list: 时间轴
    :param values list: 对应的方向
    :param timeLength int: 时间跨度,每多少毫秒求一次平均
    """
    lastTime = 0
    lastIndex = 0
    retTime = []
    retValue = []
    for i in range(0, len(times)):
        if times[i] >= lastTime + timeLength:
            avg = calGoOrientAvg(values[lastIndex:i])
            retTime.append(times[i] - 500)
            retValue.append(avg)
            lastIndex = i
            lastTime = times[i]
    return retTime, retValue


def recognizeStraight(newTime, newValue, straightTime=5):
    """
    识别直行的时间区间

    :param newTime list: 时间轴
    :param newValue list: 对应的方向值
    :param straightTime int: 直行的最短时间单位
    """
    retTime = []
    retValue = []
    retLen = []
    retOrient = []
    goLen = 0
    lastIsGo = False
    for i in range(straightTime, len(newTime)+1):
        curIsGo = judgeStraight(newValue[i - straightTime: i])
        # 判断i前面的，不包括i点
        if curIsGo and lastIsGo:  # 直行继续
            goLen += 1
        elif lastIsGo:  # 直行中断
            retValue.append(newValue[i-2])
            retTime.append(newTime[i-2])
            retLen.append(goLen)
            retOrient.append(calGoOrientAvg(newValue[i-goLen-1: i-1]))
        elif curIsGo:  # 开始检测出直行
            goLen = 5
        lastIsGo = curIsGo

    if lastIsGo:
        retValue.append(newValue[-1])
        retTime.append(newTime[-1])
        retLen.append(goLen)
        retOrient.append(calGoOrientAvg(newValue[-goLen:]))

    return retTime, retValue, retLen, retOrient
