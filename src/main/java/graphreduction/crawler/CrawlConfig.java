package main.java.graphreduction.crawler;

/**
 * Contains the configuration for the webcrawler.
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class CrawlConfig {
	static final int NUM_CRAWLERS = 10;
	static final int MAX_DEPTH = 1;
	static final int MAX_PAGES_TO_FETCH = -1; // no limit
	static final boolean INCLUDE_HTTPS_PAGES = true;
	static final boolean SHUT_DOWN_ON_EMPTY_QUEUE = true;
	static final int THREAD_MONITORING_DELAY_SEC = 3;
	static final int THREAD_SHUTDOWN_DELAY_SEC = 3;
	static final int CLEANUP_DELAY_SEC = 5;
}
