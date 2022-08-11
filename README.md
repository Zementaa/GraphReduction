# GraphReduction

# Plugins
- neo4j graph data science 1.8.3
- neosemantics 4.4.0.1
- apoc 3.5.0.19

# Configuring neo4j to use RDF data

- [Configure](https://neo4j.com/labs/neosemantics/4.0/config/)
- [Import RDF data](https://neo4j.com/labs/neosemantics/4.0/import/)
- neo4j.conf

```bash
dbms.security.procedures.unrestricted=gds.*
dbms.security.procedures.allowlist=apoc.coll.*,apoc.load.*,gds.*
```

# Vorgehen
 * Erstellen des Kookkurrenzgraphen
 * Auswahl der wichtigsten Knoten (Ranking-Klasse)
 * Markierung der wichtigsten Knoten von Hand (markNodes()-Methode)

Crawler für News:
https://github.com/fhamborg/news-please

Evening Standard Archive:
https://www.standard.co.uk/archive

# Algorithmen
## Centrality
Centrality algorithms are used to determine the importance of distinct nodes in a network.
- Betweenness
- Degree Centrality

## Community Detection
Community detection algorithms are used to evaluate how groups of nodes are clustered or partitioned, as well as their tendency to strengthen or break apart.
- Louvain
- Label Propagation
- Modularity Optimization (beta)

## Path finding
Path finding algorithms find the path between two or more nodes or evaluate the availability and quality of paths.
- Ramdom Walker (beta)