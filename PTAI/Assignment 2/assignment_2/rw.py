import os
import sys
import random
import numpy as np

def random_walk(n, d):
    """Carry out an integer random walk and return the final point we
    visit.

    """
    x = [0] * d # start at the origin
    for i in range(n):
        # choose a random dimension and take a step, eg north or south
        x[random.randrange(d)] += random.choice((-1, +1))
    return x

def random_walks(n, ds):
    """Carry out an integer random walk for each value of d (number of
    dimensions), and see how far the endpoint is from the origin.

    n: number of steps per walk
    ds: list giving numbers of dimensions
    returns: d and distance achieved for each random walk
    """

    # we will store d and distance in two columns
    results = np.zeros((len(ds), 2))
    for i, d in enumerate(ds):
        print(f"Working on d={d}")
        # np.linalg.norm is Euclidean distance to the origin
        results[i, :] = (d, np.linalg.norm(random_walk(n, d)))
    return results


def main():
    # read the random seed from the command line
    seed = int(sys.argv[1])
    # we will use the Python random module, will not use np.random, so
    # no need to run np.random.seed().
    random.seed(seed)
    ds = [1, 2, 3, 4, 5, 6, 7, 8]
    results = random_walks(1000000, ds)
    header = os.getcwd()
    np.savetxt(f"rw_results_{seed}.dat",
               results,
               fmt="%d %f",
               header=header)


if __name__ == "__main__":
    main()
