# GraphReduction
This prototype can be used, to crawl the web for a specific topic and download the articles. It then can be used to
create a co-occurrence graph from the corpus. This neo4j graph will then be analysed and reduced by any of the currently
implemented algorithms: Betwennness Centrality, Degree Centrality, Louvain, Label Propagation.

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

Crawler for News:
https://github.com/yasserg/crawler4j

Evening Standard Archive:
https://www.standard.co.uk/archive

# Configuring neo4j
Configure the config file neo4j.conf located in the directory ./conf.

```bash
  # Used Procedures must be allowed
  dbms.security.procedures.unrestricted=gds.*
  dbms.security.procedures.allowlist=apoc.coll.*,apoc.load.*,gds.*

  # Enable this to be able to upgrade a store from an older version.
  dbms.allow_upgrade=true
```

# Prototype
- Create co-occurrence graph ([NLP-Toolbox](https://www.mario-kubek.de/lectures/The_Hagen_NLPToolbox_NLIR2021.pdf))
- Choose important nodes (by hand, csv-file)
- Mark those nodes
- Choose an algorithm
- Reduce graph

# Algorithms
## Centrality
Centrality algorithms are used to determine the importance of distinct nodes in a network.
- Betweenness
- Degree Centrality

## Community Detection
Community detection algorithms are used to evaluate how groups of nodes are clustered or partitioned, as well as their
tendency to strengthen or break apart.
- Louvain
- Label Propagation

### Community Detection & Centrality
1. Use a community detection algorithm.
2. Calculate the score with a centrality measure

### Community Detection & Degree calculation
1. Use a community detection algorithm.
2. Calculate the degree within a community or to other communities

## TBD
Java Doc ergänzen
