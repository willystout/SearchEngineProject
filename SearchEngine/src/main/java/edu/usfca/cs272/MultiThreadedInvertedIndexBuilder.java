package edu.usfca.cs272;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A builder for the Thread safe inverted index
 */
public class MultiThreadedInvertedIndexBuilder {

	/**
	 * The class that workers use to add data to the ThreadSafe Inverted Index
	 */
	private static class Task implements Runnable {

		/**
		 * The index to process
		 */
		private final ThreadSafeInvertedIndex theIndex;

		/**
		 *  The path of the file to add
		 */
		private final Path thePath;

		/**
		 * Initializes this task.
		 * @param path the path of the file to read
		 * @param index the index to write to
		 */
		public Task(Path path, ThreadSafeInvertedIndex index) {
			this.thePath = path;
			this.theIndex = index;
		}

		@Override
		public void run() {
			try {
				InvertedIndex localIndex = new InvertedIndex();
				InvertedIndexBuilder.addFile(thePath, localIndex);
				theIndex.addDistinct(localIndex);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	/**
	 * This method finds out wether or not this file is a directory or not and adds
	 * the file to the index if it is not empty and not a directory
	 *
	 * @param path  Path of the file needed to be built
	 * @param index the inverted index holding the counts and inverted index
	 * @param workers The work queue to use
	 * @throws IOException If an IO exception occurs
	 */
	public static void buildPath(Path path, ThreadSafeInvertedIndex index, WorkQueue workers) throws IOException {
		if (Files.isDirectory(path)) {
			traverseDirectory(path, index, workers);
		} else {
			Task task = new Task(path, index);
			workers.execute(task);
		}
		workers.finish();
	}

	/**
	 * traverses the directory and gives the file to build file
	 *
	 * @param path  Path of the file needed to traverse
	 * @param index the inverted index holding the counts and inverted index
	 * @param workers The work queue to do
	 * @throws IOException If an IO exception occurs
	 */
	public static void traverseDirectory(Path path, ThreadSafeInvertedIndex index, WorkQueue workers)
			throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
			for (Path file : listing) {
				if (Files.isDirectory(file)) {
					traverseDirectory(file, index, workers);
				} else if (InvertedIndexBuilder.isTextFile(file)) {
					Task task = new Task(file, index);
					workers.execute(task);
				}
			}
		}
	}

}
