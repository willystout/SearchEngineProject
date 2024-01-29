package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.usfca.cs272.InvertedIndex.SearchResult;

/**
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class JsonWriter {
	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {
		var iterator = elements.iterator();
		writer.write('[');
		if (iterator.hasNext()) {
			indent++;
			writeArrayHelper(iterator.next().toString(), writer, indent);
			while (iterator.hasNext()) {
				writer.write(',');
				writeArrayHelper(iterator.next().toString(), writer, indent);
			}
			indent--;
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write(']');
	}

	/**
	 * this method writes the new line, indent and the given key for the write array
	 * method
	 *
	 * @param key    the element which you want to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO exception occurs
	 *
	 */
	public static void writeArrayHelper(String key, Writer writer, int indent) throws IOException {
		writer.write('\n');
		writeIndent(writer, indent);
		writer.write(key);
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		var iterator = elements.iterator();
		writer.write("[");
		if (iterator.hasNext()) {
			indent++;
			writeArrayObjectsHelper(iterator.next(), writer, indent);
			while (iterator.hasNext()) {
				writer.write(",");
				writeArrayObjectsHelper(iterator.next(), writer, indent);
			}
			indent--;
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]");
	}

	/**
	 * helps write an arrayObject, writing a new line, an indent and an object
	 *
	 * @param entry  the entry to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an ioexception occurs
	 */
	public static void writeArrayObjectsHelper(Map<String, ? extends Number> entry, Writer writer, int indent)
			throws IOException {
		writer.write("\n");
		writeIndent(writer, indent);
		writeObject(entry, writer, indent);
	}

	/**
	 * writes the entry for the write object method
	 *
	 * @param iterator the entry set iterator going through the map
	 * @param writer   the writer to use
	 * @param indent   the amount of indent to use
	 * @throws IOException throws an io exception if cannot write quote
	 */
	public static void writeEntry(Iterator<? extends Entry<String, ? extends Number>> iterator, Writer writer,
			int indent) throws IOException {
		var entry = iterator.next();
		writer.write("\n");
		writeQuote(entry.getKey(), writer, indent);
		writer.write(": " + entry.getValue());

	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Writes the inverted index, which includes a map of all words found in files
	 * given, how many times that word occured, and its location in the file.
	 *
	 * @param index contains the inverted index and counts index
	 * @return a string of the inverted index
	 * @throws IOException if write inverted index fails
	 */
	public static String writeInvertedIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> index) throws IOException {
		StringWriter writer = new StringWriter();
		writeInvertedIndex(index, writer, 0);
		return writer.toString();
	}

	/**
	 * Writes the inverted index, which includes a map of all words found in files
	 * given, how many times that word occured, and its location in the file.
	 *
	 * @param index contains the inverted index and counts index
	 * @param path  path of the file to write to
	 * @throws IOException if an IO error occurs
	 */
	public static void writeInvertedIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> index, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeInvertedIndex(index, writer, 0);
		}
	}

	/**
	 * Writes the inverted index, which includes a map of all words found in files
	 * given, how many times that word occured, and its location in the file.
	 *
	 * @param index  the inverted index and counts index
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *               inner elements are indented by one, and the last bracket is
	 *               indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeInvertedIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> index, Writer writer, int indent)
			throws IOException {
		var iterator = index.entrySet().iterator();
		writer.write("{");
		if (iterator.hasNext()) {
			indent++;
			writeNestedObject(index, iterator, writer, indent);
			while (iterator.hasNext()) {
				writer.write(",");
				writeNestedObject(index, iterator, writer, indent);
			}
			indent--;
		}
		writer.write("\n");
		writer.write("}");
	}

	/**
	 * Writes a nested array, with its key and its array
	 *
	 * @param iterator the element to write that holds the values of the key and its
	 *                 associated value
	 * @param writer   the writer to use
	 * @param indent   the amount of indent needed
	 * @throws IOException if an IO exception occurs
	 *
	 */
	public static void writeNestedArray(
			Iterator<? extends Entry<String, ? extends Collection<? extends Number>>> iterator, Writer writer,
			int indent) throws IOException {
		var entry = iterator.next();
		writer.write("\n");
		writeQuote(entry.getKey(), writer, indent);
		writer.write(": ");
		writeArray(entry.getValue(), writer, indent);

	}

	/**
	 * writes a nested object, helping write the inverted index
	 *
	 * @param index    the inverted index and counts index
	 * @param iterator the value needed to write what is index of the inverted index
	 * @param writer   the writer to use
	 * @param indent   the amount of indent to use
	 * @throws IOException if an IO exception occurs
	 *
	 */
	public static void writeNestedObject(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> index,
			Iterator<? extends Entry<String, ? extends Map<String, ? extends Collection<? extends Number>>>> iterator,
			Writer writer, int indent) throws IOException {
		var entry = iterator.next();
		writer.write("\n");
		writeQuote(entry.getKey(), writer, indent);
		writer.write(": ");
		writeObjectArrays(entry.getValue(), writer, indent);
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent)
			throws IOException {
		var iterator = elements.entrySet().iterator();
		writer.write("{");
		if (iterator.hasNext()) {
			indent++;
			writeEntry(iterator, writer, indent);
			while (iterator.hasNext()) {
				writer.write(",");
				writeEntry(iterator, writer, indent);
			}
			indent--;
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 * @throws IOException if an io exception occurs
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements)
			throws IOException {
		StringWriter writer = new StringWriter();
		writeObjectArrays(elements, writer, 0);
		return writer.toString();
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */

	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		var iterator = elements.entrySet().iterator();
		writer.write("{");
		if (iterator.hasNext()) {
			indent++;
			writeNestedArray(iterator, writer, indent);
			while (iterator.hasNext()) {
				writer.write(",");
				writeNestedArray(iterator, writer, indent);
			}
			indent--;
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 *  Helps write the search result and returns a StringWriter
	 *
	 * @param rank The search result map
	 * @return A written search result ranking
	 * @throws IOException If an IOException occurs
	 */
	public static String writeSearchResult(Map<String, ArrayList<SearchResult>> rank) throws IOException {
		StringWriter writer = new StringWriter();
		writeSearchResult(rank, writer, 0);
		return writer.toString();

	}

	/**
	 * Helps write the search result
	 *
	 * @param rank The search result map
	 * @param path the file path to use
	 * @throws IOException If an IOException occurs
	 */
	public static void writeSearchResult(Map<String, ArrayList<SearchResult>> rank, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeSearchResult(rank, writer, 0);
		}
	}

	/**
	 * Writes the search result rank map to a file
	 *
	 * @param rank   The search result map
	 * @param writer the writer to use
	 * @param indent The number of times to indent
	 * @throws IOException If an IOException occurs
	 */
	public static void writeSearchResult(Map<String, ArrayList<SearchResult>> rank, Writer writer, int indent)
			throws IOException {
		var iterator = rank.entrySet().iterator();
		writer.write("{");
		if (iterator.hasNext()) {
			indent++;
			writeSearchResultCollection(writer, indent, iterator);
			while (iterator.hasNext()) {
				writer.write(",");
				writeSearchResultCollection(writer, indent, iterator);
			}
			indent--;
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * writes a collection of search results
	 *
	 * @param writer The writer to use
	 * @param indent The number of times to indent
	 * @param iterator the iterator for the arraylist of search results
	 * @throws IOException If an IOException occurs
	 */
	public static void writeSearchResultCollection(Writer writer, int indent,
			Iterator<Entry<String, ArrayList<SearchResult>>> iterator) throws IOException {
		var key = iterator.next();
		writer.write("\n");
		writeQuote(key.getKey(), writer, indent);
		writer.write(": [");
		indent++;
		var search = key.getValue().iterator();
		if (search.hasNext()) {
			writeSingleSearchResult(writer, indent, search);
			while (search.hasNext()) {
				writer.write(",");
				writeSingleSearchResult(writer, indent, search);
			}
		}
		indent--;
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]");
	}

	/**
	 * helps write the search result ranking by writing a single search result
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @param search The search result object to write
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSingleSearchResult(Writer writer, int indent, Iterator<SearchResult> search)
			throws IOException {
		writer.write("\n");
		var toWrite = search.next();
		writeIndent(writer, indent);
		writer.write("{");
		writer.write("\n");
		indent++;
		writeQuote("count", writer, indent);
		writer.write(": " + toWrite.getNumMatches() + ",");
		writer.write("\n");
		writeQuote("score", writer, indent);
		String formatted = String.format("%.8f", toWrite.getScore());
		writer.write(": " + formatted + ",");
		writer.write("\n");
		writeQuote("where", writer, indent);
		writer.write(": ");
		writer.write("\"" + toWrite.getLocationOfFile() + "\"");
		writer.write("\n");
		indent--;
		writeIndent(writer, indent);
		writer.write("}");
	}
}
