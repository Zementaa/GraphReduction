package main.java.graphreduction.crawler;

public class CrawlConfig {
	final int NUM_CRAWLERS = 10;
	final int MAX_DEPTH = 1;
	final int MAX_PAGES_TO_FETCH = -1; // no limit
	final boolean INCLUDE_HTTPS_PAGES = true;
	final boolean SHUT_DOWN_ON_EMPTY_QUEUE = true;
	final int THREAD_MONITORING_DELAY_SEC = 3;
	final int THREAD_SHUTDOWN_DELAY_SEC = 3;
	final int CLEANUP_DELAY_SEC = 5;
}
