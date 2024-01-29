package edu.usfca.cs272;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import edu.usfca.cs272.SearchEngineServer.IndexBrowserServlet;
import edu.usfca.cs272.SearchEngineServer.LocationServlet;
import edu.usfca.cs272.SearchEngineServer.SearchEngineServlet;
import edu.usfca.cs272.SearchEngineServer.ShutdownServlet;

/**
 * @author William Stout
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class Driver {
	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */

	public static void main(String[] args) {
		Instant start = Instant.now();
		ArgumentParser parser = new ArgumentParser(args);
		ThreadSafeInvertedIndex multiThread = null;
		InvertedIndex invertedIndex = null;
		WorkQueue workers = null;
		WebCrawler webCrawler = null;
		int numWorkers = 5;
		int numCrawlers = 0;
		int PORT = 8080;
		boolean isThreaded = parser.hasFlag("-threads");
		QueryProcessorInterface queryProcessor = null;

		if (isThreaded || parser.hasFlag("-html") || parser.hasFlag("-server")) {
			if (parser.hasValue("-threads") && (parser.getInteger("-threads") >= 1)) {
				numWorkers = parser.getInteger("-threads");
			}
			if (parser.hasFlag("-crawl") && parser.hasValue("-crawl")) {
				numCrawlers = parser.getInteger("-crawl");
			}
			if (parser.hasValue("-server") && (parser.getInteger("-server") >= 1)) {
				PORT = parser.getInteger("-server");
			}
			workers = new WorkQueue(numWorkers);
			multiThread = new ThreadSafeInvertedIndex();
			invertedIndex = multiThread;
			queryProcessor = new MultiThreadedQueryProcessor(multiThread, parser.hasFlag("-partial"), workers);
		} else {
			invertedIndex = new InvertedIndex();
			queryProcessor = new QueryProcessor(invertedIndex, parser.hasFlag("-partial"));
		}

		if (parser.hasFlag("-html") && parser.hasValue("-html")) {
			String htmlLink = parser.getString("-html");
			try {
				if (htmlLink != null) {
					URI newURI = LinkFinder.makeUri(htmlLink);
					newURI = LinkFinder.cleanUri(newURI);
					webCrawler = new WebCrawler(workers, multiThread, numCrawlers);
					webCrawler.buildIndex(newURI.toURL());
				}
			} catch (MalformedURLException e) {
				System.err.println("Error converting link to URL. ");
			}
		}

		if (parser.hasFlag("-text") && parser.hasValue("-text")) {
			try {
				Path path = (parser.getPath("-text"));
				if ((multiThread != null) && (workers != null)) {
					MultiThreadedInvertedIndexBuilder.buildPath(path, multiThread, workers);
				} else {
					InvertedIndexBuilder.buildPath(path, invertedIndex);
				}
			} catch (FileNotFoundException e) {
				System.err.println("Please provide a filename. ");
			} catch (IOException e) {
				System.err.println("Unable to build word count and inverted index. ");
			}
		}

		if (parser.hasFlag("-query")) {
			Path output = parser.getPath("-query");
			try {
				if (output != null) {
					queryProcessor.buildQuery(output);
				}
			} catch (IOException e) {
				System.out.println("ERROR: No query provided. ");
			}
		}

		if (parser.hasFlag("-counts")) {
			Path output = parser.getPath("-counts", Path.of("counts.json"));
			try {
				invertedIndex.writeCounts(output);
			} catch (IOException e) {
				System.err.println("Unable to write the word counts to the JSON file at: " + output);
			}
		}

		if (parser.hasFlag("-results")) {
			Path output = parser.getPath("-results", Path.of("results.json"));
			try {
				queryProcessor.writeSearchRank(output);
			} catch (IOException e) {
				System.out.println("Unable to write query. ");
			}
		}

		if (parser.hasFlag("-index")) {
			Path output = parser.getPath("-index", Path.of("index.json"));
			try {
				invertedIndex.writeIndex(output);
			} catch (IOException e) {
				System.err.println("Cannot write inverted index to the JSON file at: " + output);
			}
		}

		if (parser.hasFlag("-server") && parser.hasValue("-server")) {
			try {
				Server server = new Server(PORT);
				ServletHandler handler = new ServletHandler();
				handler.addServletWithMapping(new ServletHolder(new SearchEngineServlet(multiThread, numWorkers)), "/");
				handler.addServletWithMapping(new ServletHolder(new ShutdownServlet(server)), "/shutdown");
				handler.addServletWithMapping(new ServletHolder(new LocationServlet(multiThread)), "/location");
				handler.addServletWithMapping(new ServletHolder(new IndexBrowserServlet(multiThread)), "/indexbrowser");
				server.setHandler(handler);
				server.start();
				server.join();
			} catch (Exception e) {
				System.err.println("Error starting Search Engine Server.");
			}
		}

		if (workers != null) {
			workers.shutdown();
		}

		System.out.println(Arrays.toString(args));
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}
