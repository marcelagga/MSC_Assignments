
def simulate(prog, xsize, ysize):
    """Given a "program" *prog*, run that program by "moving around" and
    tracking what happens.

    Return the number of junctions visited and the number of
    time-steps used.

    >>> simulate("NNN", 100, 100)
    (4, 3)
    >>> simulate("NNNSSS", 100, 100)
    (4, 6)
    >>> simulate("NNNESSS", 100, 100)
    (8, 7)
    >>> simulate("NNNNNSSSSS", 3, 3)
    (3, 10)
    >>> simulate(plan(10, 10), 10, 10)
    (100, 99)

    """
    
    # We need to track "current position", so we'll use integers
    # x and y for that. Initialise them.
    x, y = 0, 0

    # We need to store all the junctions we have visited. We'll use a
    # set because if we visit a junction twice, that doesn't count as
    # two. A set discards duplicates.
    visited = set()

    # We have already visited the "current" junction. Notice that
    # we're storing the position as a *tuple*. We can't use a list (ie
    # visited.add([x, y])), even though that would make sense, because
    # we can't put lists into sets.
    visited.add((x, y))

    # We need to track how many time-steps have elapsed. 
    steps = 0 
    
    for s in prog:
        
        # Execute the step s by incrementing or decrementing x or y.
        # But don't go off the grid.
        if s == "N":
            if y + 1 <= ysize - 1: # if new position is still on grid
                y += 1
        elif s == "S":
            if y - 1 >= 0:
                y -= 1
        elif s == "E":
            if x + 1 <= xsize - 1:
                x += 1
        elif s == "W":
            if x - 1 >= 0:
                x -= 1

        # One time-step has elapsed.
        steps += 1
        
        # Add the current position. If a duplicate, it will be
        # ignored.
        visited.add((x, y))
        
    return len(visited), steps

def plan(xsize, ysize):
    """Given the city limits, return a program which moves around the
    entire city. The program should be a string, eg "NNESS".

    By the way, notice that doctest will insist that the "expected"
    string, below, must be in single-quotes (''), not double-quotes
    ("").

    Our plan is: starting from the southwest, we traverse all the way
    northwards, then one step east, then all the way southwards, then
    one step east, and so on.

    >>> plan(2, 2)
    'NES'
    >>> plan(2, 3)
    'NNESS'

    """
    
    # Make an empty list. We'll convert to a string later.
    s = []

    # This loop adds the E values
    for i in range(xsize):

        # This loop adds the N or S values
        for j in range(ysize - 1):
            if i % 2 == 0:
                s.append("N")
            else:
                s.append("S")
        if i < xsize - 1:
            s.append("E")

    # Convert our list to a string.
    return "".join(s)

if __name__ == "__main__":
    import doctest
    doctest.testmod()
