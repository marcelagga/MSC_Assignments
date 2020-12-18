import numpy as np
import matplotlib.pyplot as plt
import matplotlib.cm as cm
from grammar import Grammar # assume we are in code/ directory

fname = "arithmetic2.bnf"
g = Grammar(file_name=fname)

n = 200
xs = np.linspace(0, 1, n)
ys = np.linspace(0, 1, n)
x = np.meshgrid(xs, ys) # x contains x[0] and x[1]
#ps = "np.sin(40 * x[0]) * np.sin(30 * (x[1]+0.5)) * x[0] * x[1]"

for i in range(100):
    try:
        ps = g.derive_string()
        print(ps)
        p = eval("lambda x: " + ps)
        plt.imshow(p(x), cmap="autumn")
        plt.axis('off')
        plt.show()
    except:
        print("Bad derivation...")
