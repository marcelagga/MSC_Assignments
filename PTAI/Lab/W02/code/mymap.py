def mymap(f, L):
    return [f(x) for x in L]

print(mymap(len, ["a", "cat", "and", "a", "dog"]))
