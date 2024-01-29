package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and
 * returns HTML content if possible.
 *
 * @see HttpsFetcher
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class HtmlFetcher {
	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *   resource is not html
	 *
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url) {
		return fetch(url, 0);
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)}.
	 *
	 * @param url the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *   resource is not html
	 *
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url, int redirects) {
		try {
			return fetch(new URL(url), redirects);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Calls {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *   resource is not html
	 */
	public static String fetch(URL url) {
		return fetch(url, 0);
	}

	/**
	 * Fetches the resource at the URL using HTTP/1.1 and sockets. If the status
	 * code is 200 and the content type is HTML, returns the HTML as a single
	 * string. If the status code is a valid redirect, will follow that redirect if
	 * the number of redirects is greater than 0. Otherwise, returns {@code null}.
	 *
	 * @param url the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *   resource is not html
	 *
	 * @see HttpsFetcher#openConnection(URL)
	 * @see HttpsFetcher#printGetRequest(PrintWriter, URL)
	 * @see HttpsFetcher#getHeaderFields(BufferedReader)
	 *
	 * @see String#join(CharSequence, CharSequence...)
	 *
	 * @see #isHtml(Map)
	 * @see #getRedirect(Map)
	 */
	public static String fetch(URL url, int redirects) {
		StringBuilder html = new StringBuilder();

		try (Socket socket = HttpsFetcher.openConnection(url);
				PrintWriter request = new PrintWriter(socket.getOutputStream());
				InputStreamReader input = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
				BufferedReader response = new BufferedReader(input);) {

			printGetRequest(request, url);

			Map<String, List<String>> headers = HttpsFetcher.getHeaderFields(response);

			if ((getStatusCode(headers) == 200) && isHtml(headers)) {
				html.append("");
				List<String> content = response.lines().toList();
				html.append("\n");
				for (String line : content) {
					if (line != null) {
						html.append(line);
						html.append("\n");
					}
				}
			}

			/**
			 * If the status code is a valid redirect, will follow that redirect if
			 * the number of redirects is greater than 0. Otherwise, returns {@code null}.
			 *
			 **/
			if ((getRedirect(headers) != null) && (redirects > 0)) {
				html.append(""); // TODO Remove
				return fetch(getRedirect(headers), redirects - 1);
			}

		} catch (IOException e) {
			html = null; // TODO return null;
		}

		return html.toString();
	}

	/**
	 * If the HTTP status code is between 300 and 399 (inclusive) indicating a
	 * redirect, returns the first redirect location if it is provided. Otherwise
	 * returns {@code null}.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the first redirected location if the headers indicate a redirect
	 */
	public static String getRedirect(Map<String, List<String>> headers) {
		int statusCode = getStatusCode(headers);
		if ((statusCode >= 300) && (statusCode <= 399) && (headers.get("Location") != null)) {
			String code = headers.get("Location").get(0).toString();
			return code;
		}
		return null;

	}

	/**
	 * Parses the HTTP status code from the provided HTTP headers, assuming the
	 * status line is stored under the {@code null} key.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the HTTP status code or -1 if unable to parse for any reasons
	 */
	public static int getStatusCode(Map<String, List<String>> headers) {
		if (headers.containsKey(null)) {
			return Integer.parseInt(headers.get(null).get(0).substring(9, 12));
		}
		return -1;

	}

	/**
	 * Returns {@code true} if and only if there is a "Content-Type" header and the
	 * first value of that header starts with the value "text/html"
	 * (case-insensitive).
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return {@code true} if the headers indicate the content type is HTML
	 */
	public static boolean isHtml(Map<String, List<String>> headers) {
		if (headers.containsKey("Content-Type")) {
			if (headers.get("Content-Type") != null) {
				for (String header : headers.get("Content-Type")) {
					if (header.contains("text/html")) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Writes a simple HTTP GET request to the provided socket writer.
	 *
	 * @param writer a writer created from a socket connection
	 * @param url the url to fetch via the socket connection
	 * @throws IOException if unable to write request to socket
	 */
	public static void printGetRequest(PrintWriter writer, URL url) throws IOException {
		String host = url.getHost();
		String resource = url.getFile().isBlank() ? "/" : url.getFile();

		writer.printf("GET %s HTTP/1.1\r\n", resource);
		writer.printf("Host: %s\r\n", host);
		writer.printf("Connection: close\r\n");
		writer.printf("\r\n");
		writer.flush();
	}
}
