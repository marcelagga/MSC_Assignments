
# load as before
import numpy as np
import scipy.io as sio
filename = "../data/ECG_MLII_1_NSR_100m_0.mat"
d = sio.loadmat(filename)
x = d["val"]
x = x.flatten()

# diff with prepend=x[0], ie duplicate the initial value
xd = np.diff(x, prepend=x[0])
# consider both upward and downward jumps
xda = np.abs(xd)
# find location of largest jump
idx = np.argmax(xda)
# print location and before and after values
print(idx, x[idx], x[idx+1])
