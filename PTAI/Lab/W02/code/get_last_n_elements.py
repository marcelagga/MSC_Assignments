def get_last_n_elements(s, n):
    if n > len(s):
        raise ValueError(f"Can't return {n} elements from {s} of length {len(s)}")
    return s[-n:]

print(get_last_n_elements("abcde", 2))

print(get_last_n_elements("abcde", 7))
