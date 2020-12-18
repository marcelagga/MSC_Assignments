import numpy as np
x = np.array([-3, -2, -1, 0, 1, 2, 3])
print(x)
x[x < 0] = 0
print(x)

