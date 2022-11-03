package main.java.graphreduction.core;

/**
 * The ReductionConfig Class specifies all the configuration that the reduction program needs.
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class ReductionConfig {

	static final String GRAPH_NAME = "ukraine";

	static final int NEO4J_PORT = 7687;

	static final String NEO4J_URI = "bolt://localhost:"+NEO4J_PORT;

	static final String NEO4J_USER = "neo4j";

	static final String NEO4J_PWD = "password";

	static final String NEWSPAPER_DIRECTORY = "src/test/resources/crawler4j/papers/";

	static final String PATH_TO_MARKED_NODES_LIST = "src/main/resources/input/marked_nodes.csv";

	static final boolean CRAWL_FIRST = false;

	// WARNING - permanently deletes nodes - make sure to have a copy
	static final boolean DELETION_ACTIVATED = false; // set to true to DELETE nodes at the end of the algorithm

	static final float THRESHOLD = 0.10F;

	public enum Algorithms
	{
		BETWEENNESS("betweenness"), DEGREE("degree"), LOUVAIN("louvain"),
		LABEL_PROPAGATION("labelP"), WITHIN("within"), OUTSIDE("outside");
		private final String text;

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
		private final String text;

		Modes(String text) {
			this.text = text;
		}
		public String getText() {
			return text;
		}
	}

	public enum ReductionCriteria
	{
		LEAST_SCORE("least_score_percent"), NOT_IN_TOP("not_in_top_percent"), UNDER_SCORE("under_score");
		private final String text;

		ReductionCriteria(String text) {
			this.text = text;
		}
		public String getText() {
			return text;
		}
	}
}
