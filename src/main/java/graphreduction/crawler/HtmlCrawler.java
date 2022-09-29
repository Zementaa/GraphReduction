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

public class HtmlCrawler extends WebCrawler {

	private final static Pattern EXCLUSIONS
			= Pattern.compile(".*(\\.(css|js|xml|gif|jpg|png|mp3|mp4|zip|gz|pdf))$");

	private CrawlerStatistics stats;

	public HtmlCrawler(CrawlerStatistics stats) {
		this.stats = stats;
	}

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String urlString = url.getURL().toLowerCase();
		return !EXCLUSIONS.matcher(urlString).matches()
				&& (urlString.startsWith("https://www.standard.co.uk/archive")
				|| (urlString.startsWith("https://www.standard.co.uk/news/world")
				&& Pattern.compile(Pattern.quote("Ukraine"), Pattern.CASE_INSENSITIVE).matcher(urlString).find())
				|| (urlString.startsWith("https://www.standard.co.uk/news/politics")
				&& Pattern.compile(Pattern.quote("Ukraine"), Pattern.CASE_INSENSITIVE).matcher(urlString).find()));
	}

	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		stats.incrementProcessedPageCount();

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();
			stats.incrementTotalLinksCount(links.size());

			if(!htmlParseData.getTitle().matches("Archive")){
				try {
					String title = htmlParseData.getTitle().split(Pattern.quote(" |"))[0];
					String text = htmlParseData.getText().split("Register for free to continue reading")[0];
					String exportText;
					if(text.split(title).length > 2) {
						exportText = text.split(title)[2];
						String textWithoutComments = exportText.replaceAll(" VIEW  COMMENTS ", "");
						String textWithNewLines = textWithoutComments.replaceAll(Pattern.quote(". "), "." + System.lineSeparator());
						exportToTxtFile(textWithNewLines, title);
						logger.info("exported paper: " + title);
						stats.incrementDownloadedPagesCount();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void exportToTxtFile(String text, String fileName) throws IOException {
		File crawlOutput = new File("src/test/resources/crawler4j/papers/" + fileName + ".txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(crawlOutput));
		writer.write(text);

		writer.close();
	}
}