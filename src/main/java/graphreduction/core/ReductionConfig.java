package main.java.graphreduction.core;

public class ReductionConfig {

	static final String GRAPH_NAME = "ukraine";

	static final int NEO4J_PORT = 7687;

	static final String NEO4J_URI = "bolt://localhost:"+NEO4J_PORT;

	static final String NEO4J_USER = "neo4j";

	static final String NEO4J_PWD = "password";

	static final String NEWSPAPER_DIRECTORY = "src/test/resources/crawler4j/papers/";

	static final String PATH_TO_MARKED_NODES_LIST = "src/main/resources/input/marked_nodes.csv";

	public enum Algorithms
	{
		BETWEENNESS("betweenness"), DEGREE("degree"), LOUVAIN("louvain"), LABEL_PROPAGATION("labelP"), WITHIN("within"), OUTSIDE("outside");

		private String text;

		Algorithms(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	public enum Modes
	{
		STREAM("stream"), WRITE("write");

		private String text;

		Modes(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}
}
