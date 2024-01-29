package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The data structure that holds the inverted index and counts Map
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	/**
	 * The multi reader lock to use
	 */
	private final MultiReaderLock lock;

	/**
	 * initializes the inverted index: the index and the word count
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new MultiReaderLock();
	}

	@Override
	public void addAll(List<String> words, String location, int start) {
		lock.writeLock().lock();
		try {
			super.addAll(words, location, start);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addDistinct(InvertedIndex other) {
		lock.writeLock().lock();
		try {
			super.addDistinct(other);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addToIndex(String word, String location, int position) {
		lock.writeLock().lock();
		try {
			super.addToIndex(word, location, position);
		} finally {
			lock.writeLock().unlock();
		}

	}

	@Override
	public int fileCounts(String location) {
		lock.readLock().lock();
		try {
			return super.fileCounts(location);
		} finally {
			lock.readLock().unlock();
		}

	}

	@Override
	public boolean hasCount(String location) {
		lock.readLock().lock();
		try {
			return super.hasCount(location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean hasLocation(String word, String location) {
		lock.readLock().lock();
		try {
			return super.hasLocation(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean hasPosition(String word, String location, Integer position) {
		lock.readLock().lock();
		try {
			return super.hasPosition(word, location, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean hasWord(String word) {
		lock.readLock().lock();
		try {
			return super.hasWord(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int numCounts() {
		lock.readLock().lock();
		try {
			return super.numCounts();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int numLocations(String word) {
		lock.readLock().lock();
		try {
			return super.numLocations(word);
		} finally {
			lock.readLock().unlock();
		}

	}

	@Override
	public int numPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.numPositions(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int numWords() {
		lock.readLock().lock();
		try {
			return super.numWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<SearchResult> searchExact(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.searchExact(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<SearchResult> searchPartial(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.searchPartial(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Map<String, Integer> viewCounts() {
		lock.readLock().lock();
		try {
			return super.viewCounts();
		} finally {
			lock.readLock().unlock();
		}

	}

	@Override
	public Set<String> viewLocations(String word) {
		lock.readLock().lock();
		try {
			return super.viewLocations(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<Integer> viewPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.viewPositions(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> viewWords() {
		lock.readLock().lock();
		try {
			return super.viewWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void writeCounts(Path output) throws IOException {
		lock.readLock().lock();
		try {
			super.writeCounts(output);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void writeIndex(Path output) throws IOException {
		lock.readLock().lock();
		try {
			super.writeIndex(output);
		} finally {
			lock.readLock().unlock();
		}
	}
}