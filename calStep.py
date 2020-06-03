'''
数据采集
要很清楚的知道你当前这一段时间走了多少步
人眼数的也不一定对
然后加速或者减速

要求：
识别变速过程中准确的步数
轮子一圈大约2.07339m

思路：
首先设置一个步数频率间隔T
和一个幅值阈值V
然后根据这些阈值识别出来五步
    计算这五步的平均值avg，和中位数mid，最小值min
    根据avgx0.6确定新的V
    根据这些步数的频率计算平均值avg2，更新T=avg2 * 0.8
    然后再识别出新的五步
'''


def calStep(times, values):
    ret = []
    difftime = 150
    lastTime = -difftime
    for i in range(1, len(values)-1):
        v = values[i]
        if v > values[i-1] and v > values[i+1]:
            if times[i] < lastTime + difftime:
                continue
            if v < 13:
                continue
            ret.append(i)
            lastTime = times[i]
    return ret
