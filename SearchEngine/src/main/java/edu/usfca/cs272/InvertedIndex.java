package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The data structure that holds the inverted index and counts Map
 */
public class InvertedIndex {

	/**
	 * This class stores the search results of the queries from the inverted index
	 */
	public class SearchResult implements Comparable<SearchResult> {

		/**
		 * Stores the location of the file for the search result
		 */
		private final String locationOfFile;

		/**
		 * Stores the num of times a query appears in a file for the search result
		 */
		private int numMatches;

		/**
		 * Stores the score of a query of the file for the search result
		 */
		private double theScore;

		/**
		 * @param location the location of the word
		 */
		public SearchResult(String location) {
			locationOfFile = location;
			numMatches = 0;
			theScore = 0;
		}

		@Override
		public int compareTo(SearchResult o) {
			int count = Integer.compare(o.numMatches, this.numMatches);
			int path = String.CASE_INSENSITIVE_ORDER.compare(this.locationOfFile.toString(),
					o.locationOfFile.toString());
			int score = Double.compare(o.theScore, this.theScore);

			if (score == 0) {
				if (count == 0) {
					return path;
				} else {
					return count;
				}
			} else {
				return score;
			}
		}

		/**
		 * @return returns the score of a search result
		 */
		public String getFormattedScore() {
			String score = String.format("%.8f", theScore);
			return score;
		}

		/**
		 * @return returns the location of the file in the search result
		 */
		public String getLocationOfFile() {
			return locationOfFile;
		}

		/**
		 * @return returns the number of matches in a search result
		 */
		public int getNumMatches() {
			return numMatches;
		}

		/**
		 * @return returns the score of a search result
		 */
		public Double getScore() {
			return theScore;
		}

		@Override
		public String toString() {
			return locationOfFile.toString() + " " + numMatches + " " + theScore;
		}

		/**
		 * @param numPositions The number to add to the search result's number of matches
		 */
		private void updateMatches(int numPositions) {
			this.numMatches += numPositions;
			this.theScore = (double) this.numMatches / (double) counts.get(locationOfFile);
		}

	}

	/**
	 * A map of files and their associated word counts.
	 */
	private final TreeMap<String, Integer> counts;

	/**
	 * The inverted index of the word, which file it is in, and its location in that
	 * file
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * initializes the inverted index: the index and the word count
	 *
	 */
	public InvertedIndex() {
		index = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
		counts = new TreeMap<String, Integer>();
	}

	/**
	 * adds one inverted index into another inverted index
	 *
	 * @param words    Words that you want to add to another inverted index
	 * @param location location at which you want the words to be added
	 * @param start    where to start adding the words at the index
	 *
	 */
	public void addAll(List<String> words, String location, int start) {
		for (String word : words) {
			addToIndex(word, location, start);
			start++;
		}
	}

	/**
	 * Adds one inverted index to another
	 *
	 * @param other adds this inverted index to another inverted index
	 */
	public void addDistinct(InvertedIndex other) {
		for (var otherEntry : other.index.entrySet()) {
			String otherWord = otherEntry.getKey();
			var otherMap = otherEntry.getValue();
			var thisMap = this.index.get(otherWord);

			if (thisMap == null) {
				this.index.put(otherWord, otherMap);
			} else {
				for (var otherPos : otherMap.entrySet()) {
					String otherFile = otherPos.getKey();
					var otherSet = otherPos.getValue();
					var thisSet = thisMap.get(otherFile);

					if (thisSet == null) {
						thisMap.put(otherFile, otherSet);
					} else {
						thisSet.addAll(otherSet);
					}
				}
			}
		}
		for (var otherCount : other.counts.entrySet()) {
			var otherFile = otherCount.getKey();
			var otherInt = otherCount.getValue();
			var thisFile = this.counts.get(otherFile);

			if (thisFile == null) {
				this.counts.put(otherFile, otherInt);
			} else {
				thisFile += otherInt;
			}
		}
	}

	/**
	 * adds a word, a location, and a position to the index
	 *
	 * @param word     the word to add to the data structure
	 * @param location the path at which to add the data to
	 * @param position where in the data structure to add the path and the word
	 */
	public void addToIndex(String word, String location, int position) {
		var locations = index.get(word);
		if (locations == null) {
			locations = new TreeMap<>();
			index.put(word, locations);
		}

		var positions = locations.get(location);
		if (positions == null) {
			positions = new TreeSet<>();
			locations.put(location, positions);
		}
		int theCount = counts.getOrDefault(location, 0);
		if (positions.add(position)) {
			counts.put(location, (theCount + 1));
		}
	}

	/**
	 * @param location The location of the word
	 * @return Returns the word count for the file at that location
	 */
	public int fileCounts(String location) {
		return counts.get(location);

	}

	/**
	 * returns wether or not the counts map has the location given
	 *
	 * @param location the location at which you would like to chech if it has a
	 *                 count
	 * @return Checks if counts has the location key
	 */
	public boolean hasCount(String location) {
		return counts.containsKey(location);
	}

	/**
	 * returns true or false wether or not the index has the location given the word
	 * and location
	 *
	 * @param word     which word you would like to find
	 * @param location at which location you would like to find the word
	 * @return returns true if word was found at location and false if word was not
	 *         found at location
	 */
	public boolean hasLocation(String word, String location) {
		var locations = index.get(word);
		return (locations != null) && locations.containsKey(location);
	}

	/**
	 * returns true or false wether or not the position of the word given along with
	 * the location is there
	 *
	 * @param word     The word that you want to find the position of.
	 * @param location The location at which you want to find the word
	 * @param position the Integer location of the word you would like to find
	 * @return if the inverted index has a key that matches the position
	 */
	public boolean hasPosition(String word, String location, Integer position) {
		var positions = index.get(word);
		if (positions != null) {
			var locations = positions.get(location);
			if (locations != null) {
				return locations.contains(position);
			}
		}
		return false;
	}

	/**
	 * returns true or false if the word is located in the index given the word
	 *
	 * @param word which word you would like to find
	 * @return true if index has the word false if else
	 */
	public boolean hasWord(String word) {
		return index.containsKey(word);
	}

	/**
	 * returns the size of the counts map
	 *
	 * @return the size of the counts map
	 */
	public int numCounts() {
		return counts.size();
	}

	/**
	 * returns the number of locations at a given word
	 *
	 * @param word The word at which you want to know how many locations it has
	 * @return returns how many locations the word has
	 */
	public int numLocations(String word) {
		var locations = index.get(word);
		if (locations != null) {
			return locations.size();
		} else {
			return 0;
		}

	}

	/**
	 * returns the num of positions of a word in found in the index
	 *
	 * @param word     the word at which you want to know how many times it appears
	 * @param location the location at which you want to know how many times the
	 *                 word appears in the file
	 * @return returns the amount of times the word was seen at the particular
	 *         location
	 */
	public int numPositions(String word, String location) {
		var locations = index.get(word);
		if ((locations != null)) {
			var positions = locations.get(location);
			if (positions != null) {
				return positions.size();
			}
		}
		return 0;
	}

	/**
	 * returns the amount of words in the inverted index
	 *
	 * @return returns the size of the index, or how many words the index holds
	 */
	public int numWords() {
		return index.size();
	}

	/**
	 * Gets the set of queries and the type of search and returns the correct search for that query line
	 *
	 * @param queries the set of queries to search through
	 * @param isPartial the boolean flag that tells you whether or not to use partial or exact search
	 * @return returns an array list of search results
	 */
	public ArrayList<SearchResult> search(Set<String> queries, boolean isPartial) {
		if (isPartial) {
			return searchPartial(queries);
		} else {
			return searchExact(queries);
		}
	}

	/**
	 * searches through the inverted index
	 *
	 * @param queries The set of queries to search for
	 * @return returns the search rank arraylist for the queries
	 *
	 */
	public ArrayList<SearchResult> searchExact(Set<String> queries) {
		ArrayList<SearchResult> searchRank = new ArrayList<SearchResult>();
		Map<String, SearchResult> lookup = new HashMap<String, SearchResult>();
		for (String prefix : queries) {
			var locations = index.get(prefix);
			if (locations != null) {
				searchHelper(locations, lookup, searchRank);
			}
		}
		Collections.sort(searchRank);
		return searchRank;
	}

	/**
	 * @param locations the location of the file
	 * @param lookup the map that tracks whether or not a value is already in the index
	 * @param searchRank the Search Rank index to write tos
	 */
	private void searchHelper(TreeMap<String, TreeSet<Integer>> locations, Map<String, SearchResult> lookup,
			ArrayList<SearchResult> searchRank) {
		for (var location : locations.entrySet()) {
			var result = lookup.get(location.getKey());
			if (result == null) {
				result = new SearchResult(location.getKey());
				lookup.put(location.getKey(), result);
				searchRank.add(result);
			}
			result.updateMatches(location.getValue().size());
		}
	}

	/**
	 * @param queries The queries to find the search result of
	 * @return returns an arraylist of search results to add to the search rank
	 *         index
	 */
	public ArrayList<SearchResult> searchPartial(Set<String> queries) {
		ArrayList<SearchResult> searchRank = new ArrayList<SearchResult>();
		Map<String, SearchResult> lookup = new HashMap<String, SearchResult>();
		for (String prefix : queries) {
			for (var word : index.tailMap(prefix).entrySet()) {
				if (word.getKey().startsWith(prefix)) {
					var locations = index.get(word.getKey());
					searchHelper(locations, lookup, searchRank);
				} else {
					break;
				}
			}
		}
		Collections.sort(searchRank);
		return searchRank;
	}

	/**
	 * returns an unmodifiable map of the counts map.
	 *
	 * @return An unmodifiable map of the counts index
	 */
	public Map<String, Integer> viewCounts() {
		return Collections.unmodifiableMap(counts);
	}

	/**
	 * retuns an unmodifiable view of the locations of the specified word
	 *
	 * @param word at which you want to see which files it has
	 * @return returns a unmodifiable set of the files
	 */
	public Set<String> viewLocations(String word) {
		var locations = index.get(word);
		if (locations != null) {
			return Collections.unmodifiableSet(index.get(word).keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * returns an unmodifiable view of the positions in the index
	 *
	 * @param word     the word at which you want the locations
	 * @param location the location at which you want the positions
	 * @return the locations of where the word was found
	 */
	public Set<Integer> viewPositions(String word, String location) {
		var locations = index.get(word);
		if (locations != null) {
			var positions = locations.get(location);
			if (positions != null) {
				return Collections.unmodifiableSet(positions);
			}
		}
		return Collections.emptySet();
	}

	/**
	 * returns an ummodifiable set of words in the inverted index
	 *
	 * @return returns an unmodifiable set of the word in the inverted index
	 */
	public Set<String> viewWords() {
		return Collections.unmodifiableSet(index.keySet());
	}

	/**
	 * write the counts index using JSON writer
	 *
	 * @param output the path of the file to write the counts json file to
	 * @throws IOException if the json writer is unable to write the counts index
	 *
	 */
	public void writeCounts(Path output) throws IOException {
		JsonWriter.writeObject(counts, output);
	}

	/**
	 * Writes the index using Write inverted index in JSON writer
	 *
	 * @param output the path of the file to write to
	 * @throws IOException if json writer is unable to write the inverted index
	 *
	 */
	public void writeIndex(Path output) throws IOException {
		JsonWriter.writeInvertedIndex(index, output);
	}
}