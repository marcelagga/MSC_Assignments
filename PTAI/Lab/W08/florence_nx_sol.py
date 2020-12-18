import networkx as nx
import matplotlib.pyplot as plt

G = nx.Graph() # empty graph, undirected

# read data, add edges to graph
filename = "../data/florentine_families.csv"
f = open(filename)
for line in f:
    # line.strip() removes the newline character.
    # maybe pd.read_csv would be nicer!
    n1, n2 = line.strip().split(",")
    G.add_edge(n1, n2)

# various measures of centrality exist
bc = nx.betweenness_centrality(G)
ec = nx.eigenvector_centrality(G)
print("Family betweenness_centrality eigenvector_centrality")
for n in G.nodes:
    print(f"{n} {bc[n]:.2} {ec[n]:.2}")
    

nx.draw_networkx(G)
plt.savefig("florence_nx.pdf")
