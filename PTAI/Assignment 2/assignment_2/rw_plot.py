import numpy as np
import matplotlib.pyplot as plt



def main():
    allseeds = np.genfromtxt('rw_results_0.dat', delimiter=' ')
    for n in range(1,19):
        nextseed = np.genfromtxt('rw_results_'+str(n)+'.dat', delimiter=' ')
        allseeds = np.concatenate((allseeds,nextseed),axis=0)

    plt.scatter(allseeds[:,0], allseeds[:,1])
    plt.xlabel("Dimensions")
    plt.ylabel("Distance from origin")
    plt.savefig('rw_results.png')

if __name__ == "__main__":
    main()
