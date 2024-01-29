package edu.usfca.cs272;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map.Entry;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jetty.server.Server;

import edu.usfca.cs272.InvertedIndex.SearchResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A server to hold the search engine and its servlets.
 */
public class SearchEngineServer {

	/**
	 *  The servlet to browse the inverted index
	 */
	public static class IndexBrowserServlet extends HttpServlet {
		/**
		 * Defualt serial version UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The title of the website / Search Engine
		 */
		private static final String TITLE = "WOOGLE";

		/**
		 * The thread safe index to look through to produce search results
		 */
		private final ThreadSafeInvertedIndex multiThread;

		/**
		 * @param multiThread The index to look through
		 */
		public IndexBrowserServlet(ThreadSafeInvertedIndex multiThread) {
			this.multiThread = multiThread;
		}

		/**
		 * Displays the inverted index with the word, and its locations with the number of positions
		 *
		 * @param request The request to be handled
		 * @param response The response to be returned
		 * @throws ServletException If a ServletException occurs
		 * @throws IOException If an IOException occurs
		 */
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			String html = """
					<!DOCTYPE html>
					<html lang="en">=
					<head>
					  <meta charset="utf-8">
					  <meta name="viewport" content="width=device-width, initial-scale=1">
					  <title>%1$s</title>
					  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css">
					  <script src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/js/all.min.js" integrity="sha512-Tn2m0TIpgVyTzzvmxLNuqbSJH3JP8jm+Cy3hvHrW7ndTDcJ1w5mBiksqDBb8GpE2ksktFvDB/ykZ0mDpsZj20w==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
					</head>
					<body>
					  <section class="hero is-primary is-bold">
					    <div class="hero-body">
					      <div class="container">
					        <h1 class="title">%1$s</h1>
					      </div>
					    </div>
					  </section>
					  <section class="section">
					    <div class="container">
					      <h2 class="title">Index Browser</h2>
					<pre>
					%2$s
					</pre>
					    </div>
					  </section>
					</body>

					</html>
					""";
			StringBuilder indexout = new StringBuilder();
			for (String words : multiThread.viewWords()) {
				indexout.append("The word: " + words + "\n");
				for (String locations : multiThread.viewLocations(words)) {
					indexout.append("<a href=" + locations + ">" + locations + "</a>\n");
					indexout.append("	Number of positions: " + multiThread.numPositions(words, locations) + "\n");
				}
			}
			PrintWriter out = response.getWriter();
			out.printf(html, TITLE, indexout.toString());
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
		}
	}

	/**
	 * The servlet to handle browse the locations of the inverted index
	 */
	public static class LocationServlet extends HttpServlet {

		/**
		 * Defualt serial version UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The title of the website / Search Engine
		 */
		private static final String TITLE = "WOOGLE";

		/**
		 * The thread safe index to look through to produce search results
		 */
		private final ThreadSafeInvertedIndex multiThread;

		/**
		 * @param multiThread The index to look through
		 */
		public LocationServlet(ThreadSafeInvertedIndex multiThread) {
			this.multiThread = multiThread;
		}

		/**
		 * Displays a index of every location in the inverted index with its associated wordcount
		 *
		 * @param request The request to be handled
		 * @param response The response to be returned
		 * @throws ServletException If a ServletException occurs
		 * @throws IOException If an IOException occurs
		 */
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			String html = """
					<!DOCTYPE html>
					<html lang="en">=
					<head>
					  <meta charset="utf-8">
					  <meta name="viewport" content="width=device-width, initial-scale=1">
					  <title>%1$s</title>
					  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css">
					  <script src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/js/all.min.js" integrity="sha512-Tn2m0TIpgVyTzzvmxLNuqbSJH3JP8jm+Cy3hvHrW7ndTDcJ1w5mBiksqDBb8GpE2ksktFvDB/ykZ0mDpsZj20w==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
					</head>
					<body>
					  <section class="hero is-primary is-bold">
					    <div class="hero-body">
					      <div class="container">
					        <h1 class="title">%1$s</h1>
					      </div>
					    </div>
					  </section>
					  <section class="section">
					    <div class="container">
					      <h2 class="title">Location Browser</h2>
					<pre>
					%2$s
					</pre>
					    </div>
					  </section>
					</body>

					</html>
					""";
			StringBuilder indexout = new StringBuilder();
			for (Entry<String, Integer> locations : multiThread.viewCounts().entrySet()) {
				String theLocation = locations.getKey();
				indexout.append("<a href=" + theLocation + ">" + theLocation + "</a>\n");
				indexout.append("Word Count: " + locations.getValue() + "\n");
			}
			PrintWriter out = response.getWriter();
			out.printf(html, TITLE, indexout.toString());
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
		}
	}

	/**
	 * Servlet to GET handle requests to /search
	 */
	public static class SearchEngineServlet extends HttpServlet {

		/**
		 * Defualt serial version UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The title of the website / Search Engine
		 */
		private static final String TITLE = "WOOGLE";

		/**
		 * The thread safe index to look through to produce search results
		 */
		private final ThreadSafeInvertedIndex multiThread;

		/**
		 * The number of workers to use when using the query processor.
		 */
		private final int numWorkers;

		/**
		 * The constructor for the Search Engine servlet
		 *
		 * @param index The index to look through and use
		 * @param numWorkers The number of workers to use when processing queries.
		 */
		public SearchEngineServlet(ThreadSafeInvertedIndex index, int numWorkers) {
			this.multiThread = index;
			this.numWorkers = numWorkers;
		}

		/**
		 * Displays a form where users can enter a word or phrase and recieve search results
		 *
		 * @param request The request to be handled
		 * @param response The response to be returned
		 * @throws ServletException If a ServletException occurs
		 * @throws IOException If an IOException occurs
		 */
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			String html = """
					<!DOCTYPE html>
					<html lang="en">=
					<head>
					  <meta charset="utf-8">
					  <meta name="viewport" content="width=device-width, initial-scale=1">
					  <title>%1$s</title>
					  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css">
					  <script src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/js/all.min.js" integrity="sha512-Tn2m0TIpgVyTzzvmxLNuqbSJH3JP8jm+Cy3hvHrW7ndTDcJ1w5mBiksqDBb8GpE2ksktFvDB/ykZ0mDpsZj20w==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
					</head>
					<body>
					  <section class="hero is-primary is-bold">
					    <div class="hero-body">
					      <div class="container">
					        <h1 class="title">%1$s</h1>
					      </div>
					    </div>
					  </section>
					  <section class="section">
					    <div class="container">
					      <h2 class="title">The %1$s Search Engine </h2>
						<form method="get" action="/">
						<div class="control">
							<input class="input" type="text" placeholder="Input search query here" name="word" size="50">
								<div class="control">
									<button class="button is-primary">Submit</button>
								</div>
							<div class="searchMethod">
								<div class="control">
									<label class="checkbox">
										<input type="checkbox" name="exact">
										Exact Search
									</label>
									</div>
								</div>
							</div>
						</form>
						<button class="button"><a href="/shutdown"> Shutdown Woogle </a></button>
						<form method="get" action="/">
						<div class="control">
							<input class="input" type="text" placeholder="Add new seed" name="seed" size="50">
								<div class="control">
									<button class="button is-primary">Add Seed</button>
								</div>
						</form>
					<pre>
					%2$s
					</pre>
					    </div>
					  </section>
					</body>

					</html>
					""";
			StringBuilder result = new StringBuilder();
			WorkQueue newWorkers = new WorkQueue(numWorkers);
			String newSeed = request.getParameter("seed");
			newSeed = (newSeed == null) || newSeed.isBlank() ? "" : newSeed;
			if (!newSeed.isBlank()) {
				if (multiThread.hasCount(newSeed)) {
					result.append("WARNING: Index already has location: " + newSeed + "\n");
				} else {
					try {
						URI newURI = LinkFinder.makeUri(newSeed);
						if (newURI.isAbsolute()) {
							WebCrawler webCrawler = new WebCrawler(newWorkers, multiThread, 0);
							webCrawler.buildIndex(newURI.toURL());
							newWorkers.join();
						} else {
							// TODO newSeed might have XSS issues
							result.append("Invalid URL: " + newSeed + "\n");
						}
					} catch (MalformedURLException e) {
						System.err.println("Error converting link to URL. ");
					}
				}
			}
			Instant start = Instant.now();
			MultiThreadedQueryProcessor queryProcessor = null;
			String search = request.getParameter("word");
			boolean isExact = request.getParameter("exact") != null;
			search = (search == null) || search.isBlank() ? "" : search;
			search = StringEscapeUtils.escapeHtml4(search);
			if (isExact) {
				queryProcessor = new MultiThreadedQueryProcessor(multiThread, false, newWorkers);
			} else {
				queryProcessor = new MultiThreadedQueryProcessor(multiThread, true, newWorkers);
			}
			queryProcessor.buildQuery(search);
			newWorkers.join();
			Collection<SearchResult> searchResults = queryProcessor.getSearchResults(search);
			long elapsed = Duration.between(start, Instant.now()).toMillis();
			double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
			if (!searchResults.isEmpty()) {
				result.append("Number of results: " + searchResults.size() + "\n");
				result.append("It took " + seconds + " to generate those results. \n");
				for (SearchResult searches : searchResults) {
					String location = searches.getLocationOfFile();
					String score = searches.getFormattedScore();
					double numMatches = searches.getNumMatches();
					result.append("<a href=" + location + ">" + location + "</a>\n");
					result.append("\tScore: " + score + "\n");
					result.append("\tMatches: " + numMatches + "\n");
				}
			} else {
				result.append("No results found. ");
			}
			if (newWorkers != null) {
				newWorkers.shutdown();
			}
			PrintWriter out = response.getWriter();
			out.printf(html, TITLE, result.toString());
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
		}
	}

	/**
	 * The servlet used to shutdown the search engine
	 */
	public static class ShutdownServlet extends HttpServlet {

		/**
		 * Defualt serial version UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The title of the website / Search Engine
		 */
		private static final String TITLE = "WOOGLE";

		/**
		 * The server to shutdown
		 */
		private final Server server;

		/**
		 * The constuctor for the shutdown servlet
		 *
		 * @param server The server to shutdown
		 */
		public ShutdownServlet(Server server) {
			this.server = server;
		}

		/**
		 * Displays a form where users can enter a word or phrase and recieve search results
		 *
		 * @param request The request to be handled
		 * @param response The response to be returned
		 * @throws ServletException If a ServletException occurs
		 * @throws IOException If an IOException occurs
		 */
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			String html = """
					<!DOCTYPE html>
					<html lang="en">=
					<head>
					  <meta charset="utf-8">
					  <meta name="viewport" content="width=device-width, initial-scale=1">
					  <title>%1$s</title>
					  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css">
					  <script src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/js/all.min.js" integrity="sha512-Tn2m0TIpgVyTzzvmxLNuqbSJH3JP8jm+Cy3hvHrW7ndTDcJ1w5mBiksqDBb8GpE2ksktFvDB/ykZ0mDpsZj20w==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
					</head>
					<body>
					  <section class="hero is-primary is-bold">
					    <div class="hero-body">
					      <div class="container">
					        <h1 class="title">%1$s</h1>
					      </div>
					    </div>
					  </section>
					  <section class="section">
					    <div class="container">
					      <h2 class="title">Shutdown %1$s</h2>
						<form method="get" action="/shutdown">
						<div class="control">
							<input class="input" type="text" placeholder="Input admin password here" name="word" size="50">
								<div class="control">
									<button class="button is-primary">Shutdown</button>
								</div>
						</form>
					    </div>
					  </section>
					</body>
					</html>
					""";
			String password = request.getParameter("word");
			if ((password != null) && (password.compareTo("woogleshutdown") == 0)) {
				try {
					server.stop();
					server.join();
				} catch (Exception e) {
					System.err.println("Unable to shutdown server");
				}
			}
			PrintWriter out = response.getWriter();
			out.printf(html, TITLE);
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
		}
	}
}
