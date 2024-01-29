package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Builds the inverted index and the counts map
 */
public class InvertedIndexBuilder {

	/**
	 * this method given a path and an InvertedIndex adds counts to the counts map
	 * and words to the index
	 *
	 * @param path  Path of the file needed to be added to
	 * @param index the inverted index holding the counts and inverted index
	 * @throws IOException If an IO exception occurs
	 */
	public static void addFile(Path path, InvertedIndex index) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8);) {
			int indexAt = 1;
			String line;
			Stemmer stemmer = new SnowballStemmer(ENGLISH);
			String location = path.toString();
			while ((line = reader.readLine()) != null) {
				for (String word : FileStemmer.parse(line)) {
					index.addToIndex(stemmer.stem(word).toString(), location, indexAt);
					indexAt++;
				}
			}
		}
	}

	/**
	 * This method finds out wether or not this file is a directory or not and adds
	 * the file to the index if it is not empty and not a directory
	 *
	 * @param path  Path of the file needed to be built
	 * @param index the inverted index holding the counts and inverted index
	 * @throws IOException If an IO exception occurs
	 */
	public static void buildPath(Path path, InvertedIndex index) throws IOException {
		if (Files.isDirectory(path)) {
			traverseDirectory(path, index);
		} else {
			addFile(path, index);
		}
	}

	/**
	 * returns true or false wether or not the file given is a text file.
	 *
	 * @param file The name of the file
	 * @return returns wether or not the file given ends with ".text" or ".txt"
	 */
	public static boolean isTextFile(Path file) {
		String textFile = file.toString().toLowerCase();
		return (textFile.endsWith(".text") || (textFile.endsWith(".txt")));
	}

	/**
	 * traverses the directory and gives the file to build file
	 *
	 * @param path  Path of the file needed to traverse
	 * @param index the inverted index holding the counts and inverted index
	 * @throws IOException If an IO exception occurs
	 */
	public static void traverseDirectory(Path path, InvertedIndex index) throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
			for (Path file : listing) {
				if (Files.isDirectory(file)) {
					traverseDirectory(file, index);
				} else if (isTextFile(file)) {
					addFile(file, index);
				}
			}
		}
	}
}
