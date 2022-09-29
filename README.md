# GraphReduction

# neo4j Version
- 4.4.4

# Plugins
- neo4j graph data science 1.8.3

## pom.xml - dependencies

# Configuring neo4j

- neo4j.conf

```bash
dbms.security.procedures.unrestricted=gds.*
dbms.security.procedures.allowlist=apoc.coll.*,apoc.load.*,gds.*
```

# Vorgehen
- Erstellen des Kookkurrenzgraphen (NLP-Toolbox)
- Auswahl der wichtigsten Knoten (von Hand)
- Markierung der wichtigsten Knoten (markNodes()-Methode)

Crawler für News:
https://github.com/fhamborg/news-please

Evening Standard Archive:
https://www.standard.co.uk/archive

# Algorithmen
## Centrality
Centrality algorithms are used to determine the importance of distinct nodes in a network.
- Betweenness
	- These: Alle Knoten über Score von 500
	- Untere 10 Prozent (Score) ausschließen, um mindestens 80 % der Begriffe abzudecken
	- oder top 15 % Prozent (Anzahl Knoten)
- Degree Centrality
	- Undirected, über Score von 100

## Community Detection
Community detection algorithms are used to evaluate how groups of nodes are clustered or partitioned, as well as their
tendency to strengthen or break apart.
- Louvain
- Label Propagation

## TBD
Communityalgorithmen vergeben die communities nicht unbedingt auf Basis dessen, dass alle Knoten innerhalb einer
Community verbunden sind.

Knoten in einer community die möglichst viele andere Knoten aus der Community erreichen
Knoten die möglichst viele andere Communities erreichen 

Community kleinste irrelevant?
Community mit oberen top Prozent

Community Within:
Median, Top5, Top xy%

Community toOther
Anzahl Communities, wie viele anderen sollen erreicht werden
bspw. Jede fünfte (von 60) oder 1% von 100.000

Louvain: Seed für Start-CommunityIds

Java Doc ergänzen
