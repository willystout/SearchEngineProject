package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.usfca.cs272.InvertedIndex.SearchResult;

/**
 * A multi threaded version of query processor
 */
public class MultiThreadedQueryProcessor implements QueryProcessorInterface {

	/**
	 * THe task that builds the query processor while multithreading it
	 */
	private class Task implements Runnable {

		/**
		 * The line to use
		 */
		private final String line;

		/**
		 * Initializes this task.
		 * @param line The query line
		 */
		public Task(String line) {
			this.line = line;
		}

		@Override
		public void run() {
			TreeSet<String> uniqueStems = FileStemmer.uniqueStems(line);
			var joined = String.join(" ", uniqueStems);
			synchronized (searchRank) {
				if (uniqueStems.isEmpty() || (searchRank.get(joined) != null)) {
					return;
				}
			}
			ArrayList<SearchResult> result = index.search(uniqueStems, isPartialSearch);
			synchronized (searchRank) {
				searchRank.put(joined, result);
			}
		}
	}

	/**
	 * The index to use and look through for the Search Rank
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * Determines whether or not the search rank is a partial search or not
	 */
	private final boolean isPartialSearch;

	/**
	 * The query and its search ranks
	 */
	private final TreeMap<String, ArrayList<SearchResult>> searchRank;

	/**
	 * The number of workers/threads to use
	 */
	private final WorkQueue workers;

	/**
	 *
	 * Initializes the queries for use and tells the class which kind of search this is
	 * @param newIndex The index to use and write to
	 * @param isPartial tells the query processor whether or not this is a partial search
	 * @param newWorkers The number of workers(or threads) to use
	 */
	public MultiThreadedQueryProcessor(ThreadSafeInvertedIndex newIndex, boolean isPartial, WorkQueue newWorkers) {
		workers = newWorkers;
		index = newIndex;
		isPartialSearch = isPartial;
		searchRank = new TreeMap<String, ArrayList<SearchResult>>();
	}

	@Override
	public void buildQuery(Path path) throws IOException {
		QueryProcessorInterface.super.buildQuery(path);
		workers.finish();
	}

	@Override
	public void buildQuery(String line) {
		Task task = new Task(line);
		workers.execute(task);
	}

	@Override
	public Set<String> getQueries() {
		synchronized (searchRank) {
			return Collections.unmodifiableSet(searchRank.keySet());
		}
	}

	@Override
	public Collection<SearchResult> getSearchResults(String query) {
		String joined = processLine(query);
		synchronized (searchRank) {
			if (searchRank.get(joined) != null) {
				return Collections.unmodifiableCollection(searchRank.get(joined));
			} else {
				return Collections.emptyList();
			}
		}
	}

	@Override
	public boolean hasSearchRank(String query) {
		String joined = processLine(query);
		synchronized (searchRank) {
			var searchRanks = searchRank.get(joined);
			return (searchRanks != null) && searchRank.containsKey(joined);
		}
	}

	@Override
	public int numQueries() {
		synchronized (searchRank) {
			return searchRank.keySet().size();
		}
	}

	@Override
	public int numSearchRanks(String query) {
		String joined = processLine(query);
		synchronized (searchRank) {
			var locations = searchRank.get(joined);
			if (locations != null) {
				return locations.size();
			}
		}
		return 0;
	}

	@Override
	public String processLine(String query) {
		TreeSet<String> uniqueStems = FileStemmer.uniqueStems(query);
		var joined = String.join(" ", uniqueStems);
		return joined;
	}

	@Override
	public String toString() {
		synchronized (searchRank) {
			return searchRank.toString();
		}
	}

	@Override
	public void writeSearchRank(Path output) throws IOException {
		synchronized (searchRank) {
			JsonWriter.writeSearchResult(searchRank, output);
		}
	}
}