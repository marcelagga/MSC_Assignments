# for i11 in range(1, 10):
#     for i12 in range(1, 10):
#         for i13 in range(1, 10):
#             for i21 in range(1, 10):
#                 for i22 in range(1, 10):
#                     for i23 in range(1, 10):
#                         for i31 in range(1, 10):
#                             for i32 in range(1, 10):
#                                 for i33 in range(1, 10):
#                                     if (i11+i12+i13 == i21+i22+i23 == i31+i32+i33 ==
#                                         i11+i21+i31 == i12+i22+i32 == i13+i23+i33 == 9):
#                                         print(i11, i12, i13)
#                                         print(i21, i22, i23)
#                                         print(i31, i32, i33)
#                                         print("")
            
import itertools
for p in itertools.permutations(list(range(1, 10))):
    if (p[0]+p[1]+p[2] == p[3]+p[4]+p[5] == p[6]+p[7]+p[8] ==
        p[0]+p[3]+p[6] == p[1]+p[4]+p[7] == p[2]+p[5]+p[8]):
        print(p[:3])
        print(p[3:6])
        print(p[6:])
        print("")
