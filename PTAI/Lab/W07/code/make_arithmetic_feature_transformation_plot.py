import numpy as np
import matplotlib.pyplot as plt
from sklearn.linear_model import LinearRegression

fig, axs = plt.subplots(1, 2, figsize=(6, 3))

g = np.linspace(0, 10, 100)
X = np.array([0, 1.5, 2, 4, 4.5, 5, 6, 7, 8])
y = np.array([1.5, 1.2, 1.0, 0.9, 0.9, 1.1, 1.1, 1.5, 1.6])

X1 = X.reshape(-1, 1)
y1g = LinearRegression().fit(X1, y).predict(g.reshape(-1, 1))
X2 = np.array([X, X**2]).T # add a feature
g2 = np.array([g, g**2]).T
y2g = LinearRegression().fit(X2, y).predict(g2)

axs[0].set_ylim(0, 2.5)
axs[1].set_ylim(0, 2.5)
axs[0].scatter(X, y)
axs[0].plot(g, y1g)
axs[1].scatter(X, y)
axs[1].plot(g, y2g)

plt.savefig("../img/arithmetic-feature-transformation.png")
plt.close()
