# GraphReduction


# Dependencies
- org.neo4j:neo4j:jar:4.4.4:compile
- org.neo4j.driver:neo4j-java-driver:jar:4.4.5:compile
- org.neo4j.gds:algo-common:jar:1.8.8:compile
- org.neo4j.gds:alpha-algo:jar:1.8.8:compile
- org.neo4j.gds:alpha-proc:jar:1.8.8:compile
- org.neo4j.gds:core:jar:1.8.8:compile
- org.neo4j.gds:algo:jar:1.8.8:compile
- org.neo4j.gds:proc-common:jar:1.8.8:compile
- org.neo4j.gds:proc:jar:1.8.8:compile
- edu.uci.ics:crawler4j:jar:4.4.0:compile
- org.junit.jupiter:junit-jupiter-api:jar:5.9.0:compile
- org.apache.logging.log4j:log4j-core:jar:2.19.0:compile
- org.apache.logging.log4j:log4j-api:jar:2.19.0:compile

# Configuring neo4j
Configure the config file neo4j.conf located in the directory ./conf.

```bash
  # Used Procedures must be allowed
  dbms.security.procedures.unrestricted=gds.*
  dbms.security.procedures.allowlist=apoc.coll.*,apoc.load.*,gds.*

  # Enable this to be able to upgrade a store from an older version.
  dbms.allow_upgrade=true
```

# Vorgehen
- Erstellen des Kookkurrenzgraphen ([NLP-Toolbox](https://www.mario-kubek.de/lectures/The_Hagen_NLPToolbox_NLIR2021.pdf))
- Auswahl der wichtigsten Knoten (von Hand)
- Markierung der wichtigsten Knoten (markNodes()-Methode)
- Durchführen einen der Algorithmen

Crawler für News:
https://github.com/yasserg/crawler4j

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
    - Untere 30 Prozent (Score) ausschließen, um mindestens 80 % der Begriffe abzudecken
	- oder top 15 % Prozent (Anzahl Knoten)

## Community Detection
Community detection algorithms are used to evaluate how groups of nodes are clustered or partitioned, as well as their
tendency to strengthen or break apart.
- Louvain
- Label Propagation

Community within: Top xy%
Community outside: Anzahl Communities, wie viele anderen sollen erreicht werden bspw. 20 % (von 60) oder 1 %
von 100.000

## Community Detection i.V.m. Centrality
- Zuerst Ausführung eines Community Detection Algorithmus.
- Danach Anwendung eines Centrality Algorithmus mit denselben obigen Parametern.

## TBD
Communityalgorithmen vergeben die communities nicht unbedingt auf Basis dessen, dass alle Knoten innerhalb einer
Community verbunden sind.

Knoten in einer community die möglichst viele andere Knoten aus der Community erreichen
Knoten die möglichst viele andere Communities erreichen 

Community kleinste irrelevant -> 2% Klausel -> Alle Communities, die weniger als 2 % der gesamten Knoten enthalten,
werden gelöscht. Beispiel: Es gibt 4443 Knoten, eine Community, die nur 5 Knoten enthält, fällt raus, da 5 < 88
(4443 * 2 % = 88) ist.

Louvain: Seed für Start-CommunityIds irrelevant, weil Ergebnis in Summe dasselbe ist. Unter Umständen sinnvoll, 
wenn ein Datensatz sehr heterogene Daten enthält - bspw. viele Unterthemen.

Java Doc ergänzen
