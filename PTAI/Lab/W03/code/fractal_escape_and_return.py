
def test_escape_and_return(z0, its=20):
    c = -0.065 + 0.66j # Julia constant: using 'j'
    z = z0
    def f(z): return z**2 + c # complex arithmetic is built-in
    for i in range(its):
        print("abs(z)", abs(z))
        if abs(z) > 2:
            print("outside")
            z = f(z)
        else:
            print("inside")
            z = f(z)
    return its # deem escape time equal to number of iterations, as a default
test_escape_and_return(0.4 + 0.5j, 20)

# we see that abs(z) grows huge (10**200) and then becomes NaN. When z
# == NaN, abs(z) > 2 is False! So, if we really want to test that
# points never return after escaping, the following code will work.

import math
def test_escape_and_return_fixed(z0, its=20):
    c = -0.065 + 0.66j # Julia constant: using 'j'
    z = z0
    def f(z): return z**2 + c # complex arithmetic is built-in
    for i in range(its):
        print("abs(z)", abs(z))
        if abs(z) > 2 or not math.isfinite(abs(z)):
            print("outside")
            z = f(z)
        else:
            print("inside")
            z = f(z)
    return its # deem escape time equal to number of iterations, as a default

