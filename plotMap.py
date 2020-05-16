# -*- coding:utf-8 -*-

import matplotlib.pyplot as plt
import numpy as np
import os


routeList = []
plt.subplot(121)
for index,fname in enumerate(os.listdir('routes/')):
    distance = float(fname.split('#')[1])
    color = fname.split('#')[0]
    path = os.path.join('routes', fname)
    data = np.loadtxt(path, delimiter=',')
    if color == 'k':
        plt.plot(data[:,0] + index * 5, data[:,1] + index * 10, '*-', color=color, lw=6, label='route-predict')
    else:
        label = 'route-'+color
        routeList.append([distance, color, label])
        plt.plot(data[:,0] + index * 5, data[:,1] + index * 10, '*-', color=color, lw=6, label=label)

font1 = {
'weight' : 'normal',
'size'   : 13,
}
plt.title('Map route')
plt.legend(loc='upper left', prop=font1)
plt.subplot(122)
routeList = sorted(routeList, key=lambda e: e[0], reverse=False)
plt.barh(range(len(routeList)), [ e[0] for e  in routeList] , color=''.join([ e[1] for e in routeList ]), tick_label=[ e[2] for e in routeList ])
plt.title('Route distance')
plt.show()
