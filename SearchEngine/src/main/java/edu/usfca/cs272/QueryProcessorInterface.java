package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import edu.usfca.cs272.InvertedIndex.SearchResult;

/**
 * This is an interface for both the single-threaded and multi-threaded query processor
 */
public interface QueryProcessorInterface {

	/**
	 * This method builds the query using the inverted index and a read the queries from
	 *
	 * @param path            The path at which to read the queries at
	 *                        search rank
	 * @throws IOException if the file cannot be read / does not exist
	 */
	public default void buildQuery(Path path) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8);) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				buildQuery(line);
			}
		}
	}

	/**
	 * @param line The query line to process
	 */
	public void buildQuery(String line);

	/**
	 * @return returns an unmodifiable set of queries
	 */
	public Set<String> getQueries();

	/**
	 * @param query The line to get search Results from
	 * @return A collection of unmodifiable search results
	 */
	public Collection<SearchResult> getSearchResults(String query);

	/**
	 * @param query the query that you want to check
	 * @return returns true if the query has a search result, false if otherwise
	 */
	public boolean hasSearchRank(String query);

	/**
	 * @return returns the number of queries the search rank index has
	 */
	public int numQueries();

	/**
	 * @param query the query at which you want to get the number of search ranks
	 * @return returns the number of search ranks of the given query
	 */

	public default int numSearchRanks(String query) {
		return getSearchResults(query).size();
	}

	/**
	 * @param query The query link to process
	 * @return Returns a stemmed line of queries
	 */
	public String processLine(String query);

	/**
	 * @param output The output to write the search rank to
	 * @throws IOException if IO Exception occurs
	 */
	public void writeSearchRank(Path output) throws IOException;
}
