def invert_dict(d):
    # use a dict comprehension to invert the dict
    # remember, "for k in d" iterates over the keys of the dict.
    return {d[k]: k for k in d}

# this works, eg:
d = {"a": 1, "dog": 3, "giraffe": 7}
print(invert_dict(d))

# however, we know that a dict can't contain the same key twice:
d = {"a": 1, "dog": 3, "giraffe": 7, "giraffe": 67}
print(d)

# therefore, if there are multiple entries with the same *value*, when we invert,
# only one is retained. In fact it's the *last* as it "overwrites" the others
d = {"a": 1, "dog": 3, "giraffe": 7, "cat": 3}
print(invert_dict(d))
