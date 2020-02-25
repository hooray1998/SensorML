# -*- coding:utf-8 -*-
import os
from processData import processData


folder = 'alldata'
for fname in os.listdir(folder):
    if 'Angular' in fname:
        filepath = os.path.join(folder, fname)
        process = processData(filepath)
        # break
