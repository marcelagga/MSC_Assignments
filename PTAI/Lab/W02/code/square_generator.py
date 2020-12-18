def sq():
    i = 0
    while True:
        yield i**2
        i += 1
        
# for s in sq(): 
#     print(s)


for s in sq():
    if s % 2 == 0:
        print(s)
    if s > 100:
        break
