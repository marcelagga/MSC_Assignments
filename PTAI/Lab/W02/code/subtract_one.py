def subtract_one(L):
    try:
        L[0] -= 1
    except IndexError:
        print("Empty list")
    except TypeError:
        print("type", type(L), "is immutable")
    return L
print(subtract_one([0, 1, 2, 3]))
print(subtract_one([]))
print(subtract_one((0, 1, 2, 3)))
