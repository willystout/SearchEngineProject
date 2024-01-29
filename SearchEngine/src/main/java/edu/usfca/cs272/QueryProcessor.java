package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.usfca.cs272.InvertedIndex.SearchResult;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 *
 * This class processes the query given to the driver and uses the appropriate
 * search method
 *
 */
public class QueryProcessor implements QueryProcessorInterface {

	/**
	 * The index to use and look through for the Search Rank
	 */
	private final InvertedIndex index;

	/**
	 * Determines whether or not the search rank is a partial search or not
	 */
	private final boolean isPartialSearch;

	/**
	 * The query and its search ranks
	 */
	private final TreeMap<String, ArrayList<SearchResult>> searchRank;

	/**
	 * The stemmer to use to stem the queries for processing
	 */
	private final Stemmer stemmer;

	/**
	 *
	 * Initializes the queries for use and tells the class which kind of search this is
	 * @param newIndex The index to use and write to
	 * @param isPartial tells the query processor whether or not this is a partial search
	 */
	public QueryProcessor(InvertedIndex newIndex, boolean isPartial) {
		stemmer = new SnowballStemmer(ENGLISH);
		index = newIndex;
		isPartialSearch = isPartial;
		searchRank = new TreeMap<String, ArrayList<SearchResult>>();
	}

	@Override
	public void buildQuery(String line) {
		TreeSet<String> uniqueStems = FileStemmer.uniqueStems(line, stemmer);
		String joined = String.join(" ", uniqueStems);
		if (!uniqueStems.isEmpty() && (searchRank.get(joined) == null)) {
			ArrayList<SearchResult> result = index.search(uniqueStems, isPartialSearch);
			searchRank.put(joined, result);
		}
	}

	@Override
	public Set<String> getQueries() {
		return Collections.unmodifiableSet(searchRank.keySet());
	}

	@Override
	public Collection<SearchResult> getSearchResults(String query) {
		TreeSet<String> uniqueStems = FileStemmer.uniqueStems(query, stemmer);
		var joined = String.join(" ", uniqueStems);
		if (searchRank.get(joined) != null) {
			return Collections.unmodifiableCollection(searchRank.get(joined));
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean hasSearchRank(String query) {
		TreeSet<String> uniqueStems = FileStemmer.uniqueStems(query, stemmer);
		var joined = String.join(" ", uniqueStems);
		var searchRanks = searchRank.get(joined);
		return (searchRanks != null) && searchRank.containsKey(joined);
	}

	@Override
	public int numQueries() {
		return searchRank.keySet().size();
	}

	@Override
	public String processLine(String query) {
		TreeSet<String> uniqueStems = FileStemmer.uniqueStems(query, stemmer);
		var joined = String.join(" ", uniqueStems);
		return joined;
	}

	@Override
	public String toString() {
		return searchRank.toString();
	}

	@Override
	public void writeSearchRank(Path output) throws IOException {
		JsonWriter.writeSearchResult(searchRank, output);
	}
}