package main.java.graphreduction.crawler;

import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.io.File;

/**
 * The crawl controller is in charge of all the web crawling operations.
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class CrawlController {

	/**
	 * The crawl method configures the crawler and crawls all the specified web pages.
	 *
	 * @throws Exception
	 */
	public void crawl() throws Exception {
		File crawlStorage = new File("src/test/resources/crawler4j");
		edu.uci.ics.crawler4j.crawler.CrawlConfig config = new edu.uci.ics.crawler4j.crawler.CrawlConfig();
		config.setCrawlStorageFolder(crawlStorage.getAbsolutePath());

		config.setMaxDepthOfCrawling(CrawlConfig.MAX_DEPTH);
		config.setMaxPagesToFetch(CrawlConfig.MAX_PAGES_TO_FETCH);
		config.setIncludeHttpsPages(CrawlConfig.INCLUDE_HTTPS_PAGES);
		config.setShutdownOnEmptyQueue(CrawlConfig.SHUT_DOWN_ON_EMPTY_QUEUE);
		config.setThreadMonitoringDelaySeconds(CrawlConfig.THREAD_MONITORING_DELAY_SEC);
		config.setThreadShutdownDelaySeconds(CrawlConfig.THREAD_SHUTDOWN_DELAY_SEC);
		config.setCleanupDelaySeconds(CrawlConfig.CLEANUP_DELAY_SEC);

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer= new RobotstxtServer(robotstxtConfig, pageFetcher);

		edu.uci.ics.crawler4j.crawler.CrawlController controller = new edu.uci.ics.crawler4j.crawler.CrawlController(config, pageFetcher, robotstxtServer);

		controller.addSeed("https://www.standard.co.uk/archive/2022-02-25/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-02-26/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-02-27/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-02-28/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-01/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-02/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-03/");

		controller.addSeed("https://www.standard.co.uk/archive/2022-03-04/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-05/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-06/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-07/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-08/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-09/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-10/");

		controller.addSeed("https://www.standard.co.uk/archive/2022-03-11/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-12/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-13/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-14/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-15/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-16/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-17/");

		controller.addSeed("https://www.standard.co.uk/archive/2022-03-18/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-19/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-20/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-21/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-22/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-23/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-24/");

		CrawlerStatistics stats = new CrawlerStatistics();
		edu.uci.ics.crawler4j.crawler.CrawlController.WebCrawlerFactory<HtmlCrawler> factory = () -> new HtmlCrawler(stats);

		controller.start(factory, CrawlConfig.NUM_CRAWLERS);
	}
}
