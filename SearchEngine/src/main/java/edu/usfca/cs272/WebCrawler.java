package edu.usfca.cs272;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *  The Web Crawler that crawls websites, cleans their html and adds the words in an inverted index
 */
public class WebCrawler {

	/**
	 * The task that creates tasks recursively to crawl pages, find links, and strip html to add to the index
	 */
	private class Task implements Runnable {

		/**
		 * The index to use and to add to
		 */
		private final ThreadSafeInvertedIndex index;

		/**
		 * The link to add to the index and to crawl
		 */
		private final URL link;

		/**
		 * Initializes the task to start the webcrawler
		 * @param link The query line
		 * @param index The index to write to
		 * @param linksCrawled The list of links that have been crawled
		 */
		public Task(URL link, ThreadSafeInvertedIndex index, HashSet<URL> linksCrawled) {
			this.link = link;
			this.index = index;
			synchronized (linksCrawled) {
				linksCrawled.add(link);
			}

		}

		@Override
		public void run() {
			String html = HtmlFetcher.fetch(link, 3);
			InvertedIndex localIndex = new InvertedIndex();
			if (html != null) {
				html = HtmlCleaner.stripBlockElements(html);
				ArrayList<URL> links = LinkFinder.listUrls(link, html);
				synchronized (linksCrawled) {
					for (URL subLink : links) {
						if (linksCrawled.size() >= maxCrawl) {
							break;
						}
						if (!linksCrawled.contains(subLink)) {
							Task task = new Task(subLink, index, linksCrawled);
							workers.execute(task);
						}
					}
				}
				ArrayList<String> wordsInHtml = FileStemmer.listStems(html);
				localIndex.addAll(wordsInHtml, link.toString(), 1);
				index.addDistinct(localIndex);
			}
		}
	}

	/**
	 * The index to use for the cleaned html
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * The ArrayList of all links crawled
	 */
	private final HashSet<URL> linksCrawled;

	/**
	 * The maximum amount of links to crawl
	 */
	private final int maxCrawl;

	/**
	 * The number of workers/threads to use
	 */
	private final WorkQueue workers;

	/**
	 * Initializes the WebCrawler
	 *
	 * @param workers The work queue to use
	 * @param index The index to add to
	 * @param maxCrawl Max number of links to crawl
	 */
	public WebCrawler(WorkQueue workers, ThreadSafeInvertedIndex index, int maxCrawl) {
		this.workers = workers;
		this.index = index;
		this.maxCrawl = maxCrawl;
		this.linksCrawled = new HashSet<URL>();
	}

	/**
	 * Builds the inverted index and calls the task method to crawl links.
	 *
	 * @param link The URL to use to crawl and to build the index
	**/
	public void buildIndex(URL link) {
		Task task = new Task(link, index, linksCrawled);
		workers.execute(task);
		workers.finish();
	}
}
