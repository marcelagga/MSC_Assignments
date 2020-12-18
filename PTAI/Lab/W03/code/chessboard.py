import numpy as np
x = np.linspace(1, 8, 8).astype(int)
x, y = np.meshgrid(x, x)
z = (x + y) % 2
print(z)
