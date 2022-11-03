package main.java.graphreduction.crawler;

/**
 * Statistics for the crawling.
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class CrawlerStatistics {

    private int processedPageCount = 0;
    private int totalLinksCount = 0;
    private int downloadedPagesCount = 0;

    public void incrementProcessedPageCount() {
        processedPageCount++;
    }

    public void incrementDownloadedPagesCount() {
        downloadedPagesCount++;
    }

    public void incrementTotalLinksCount(int linksCount) {
        totalLinksCount += linksCount;
    }

    public int getProcessedPageCount() {
        return processedPageCount;
    }

    public int getTotalLinksCount() {
        return totalLinksCount;
    }

    public int getDownloadedPagesCount() {
        return downloadedPagesCount;
    }
}
