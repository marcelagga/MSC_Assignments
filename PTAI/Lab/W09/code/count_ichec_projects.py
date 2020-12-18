import re
s = open("../data/ichec_projects_scrape.txt").read()

# match eg ngcom018c, ulphy033a, and extract the four parts
L = re.findall(r"([a-z]{2})([a-z]{3})([0-9]{3})([a-c])", s)
n = 0
for uni, subj, number, Class in L:
    if subj == "com" and Class == "c":
        print(uni, subj, number, Class)
        n += 1
print(n)
