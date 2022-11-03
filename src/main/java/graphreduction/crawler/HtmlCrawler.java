package main.java.graphreduction.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The HtmlCrawler Class extends the <a href="#{@link}">{@link WebCrawler}</a>.
 * Excluded from the crawling are all files different from text.
 * It is specified which websites should be visited and the result is exported to a text file.
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class HtmlCrawler extends WebCrawler {

	private static final Pattern EXCLUSIONS
			= Pattern.compile(".*(\\.(css|js|xml|gif|jpg|png|mp3|mp4|zip|gz|pdf))$");

	private final CrawlerStatistics stats;

	public HtmlCrawler(CrawlerStatistics stats) {
		this.stats = stats;
	}

	/**
	 * Here, the pattern of websites that should be visited is specified.
	 *
	 * @param referringPage
	 *           The Page in which this url was found.
	 * @param url
	 *            the url which we are interested to know whether it should be
	 *            included in the crawl or not.
	 * @return true or false
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String urlString = url.getURL().toLowerCase();
		return !EXCLUSIONS.matcher(urlString).matches()
				&& (urlString.startsWith("https://www.standard.co.uk/archive")
					|| (urlString.startsWith("https://www.standard.co.uk/news/world")
						&& (Pattern.compile(Pattern.quote("Ukraine"), Pattern.CASE_INSENSITIVE).matcher(urlString).find()
							|| Pattern.compile(Pattern.quote("Russia"), Pattern.CASE_INSENSITIVE).matcher(urlString).find()
							|| Pattern.compile(Pattern.quote("Putin"), Pattern.CASE_INSENSITIVE).matcher(urlString).find()
							|| Pattern.compile(Pattern.quote("Selenskyj"), Pattern.CASE_INSENSITIVE).matcher(urlString).find()))
					|| (urlString.startsWith("https://www.standard.co.uk/news/politics")
						&& (Pattern.compile(Pattern.quote("Ukraine"), Pattern.CASE_INSENSITIVE).matcher(urlString).find()
							|| Pattern.compile(Pattern.quote("Russia"), Pattern.CASE_INSENSITIVE).matcher(urlString).find()
							|| Pattern.compile(Pattern.quote("Putin"), Pattern.CASE_INSENSITIVE).matcher(urlString).find()
							|| Pattern.compile(Pattern.quote("Selenskyj"), Pattern.CASE_INSENSITIVE).matcher(urlString).find())));
	}

	/**
	 * Every visited page is considered in the statistics and the text is downloaded.
	 *
	 * @param page
	 *            the page object that is just fetched and parsed.
	 */
	@Override
	public void visit(Page page) {
		stats.incrementProcessedPageCount();

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();
			stats.incrementTotalLinksCount(links.size());

			if(!htmlParseData.getTitle().matches("Archive")){
				String title = htmlParseData.getTitle().split(Pattern.quote(" |"))[0];
				String text = htmlParseData.getText().split("Register for free to continue reading")[0];
				String exportText;
				if(text.split(title).length > 2) {
					exportText = text.split(title)[2];
					String textWithoutComments = exportText.replaceAll(" VIEW {2}COMMENTS ", "");
					String textWithNewLines = textWithoutComments.replaceAll(Pattern.quote(". "), "." + System.lineSeparator());
					exportToTxtFile(textWithNewLines, title);
					logger.info("exported paper: {}", title);
					stats.incrementDownloadedPagesCount();
				}
			}
		}
	}

	/**
	 * Creates a local text file that contains the text from the crawled website.
	 *
	 * @param text Text of the website that is copied into a local text file
	 * @param fileName Name of the text file
	 */
	private void exportToTxtFile(String text, String fileName) {
		File crawlOutput = new File("src/test/resources/crawler4j/papers/" + fileName + ".txt");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(crawlOutput));
			writer.write(text);
			writer.close();
		} catch (IOException e) {
			logger.error("IOException: {}", e.getMessage());
		}
	}
}