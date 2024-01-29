package edu.usfca.cs272;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds HTTP(S) URLs from the anchor tags within HTML code.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class LinkFinder {
	/**
	 * Attempts to remove the fragment component (if present) from a URI and then
	 * return the normalized URI. Also ensures that a hierarchical URI has the
	 * default path "/" if one is missing.
	 *
	 * @param uri the URI to clean and normalize
	 * @return cleaned (if possible) and normalized URI
	 *
	 * @see URI#normalize()
	 */
	public static URI cleanUri(URI uri) {
		URI normal = uri.normalize();
		URI clean = null;

		String fragment = normal.getFragment();
		String path = normal.normalize().getPath();

		try {
			if (!normal.isOpaque() && ((path == null) || path.isBlank())) {
				clean = new URI(normal.getScheme(), normal.getAuthority(), "/", normal.getQuery(), null);
			} else if (fragment != null) {
				clean = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
			}
		} catch (URISyntaxException e) {
			clean = null;
		}
		// return cleaned version if created, otherwise normalized version
		return clean != null ? clean : normal;
	}

	/**
	 * Attempts to create a normalized absolute URL from the provided base URL and
	 * link text. If fails for any reason, will return null.
	 *
	 * @param base the base URL the link text was found on
	 * @param href the link text (usually from an anchor tag href attribute)
	 * @return the normalized absolute URL or {@code null}
	 */
	public static URL convertUrl(URL base, String href) {
		URI uri = makeUri(href);
		URL url = null;
		if (uri != null) {
			try {
				if (!uri.isOpaque() && !uri.isAbsolute()) {
					URI parent = cleanUri(base.toURI());
					uri = parent.resolve(uri);
				}
				url = cleanUri(uri).toURL();
			} catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
				url = null;
			}
		}
		return url;
	}

	/**
	 * Finds all the valid HTTP(S) links in the HREF attribute of the anchor tags in
	 * the provided HTML. The links will be converted to an absolute URL using the
	 * base URL and normalized (removing fragments and encoding special characters
	 * as necessary).
	 *
	 * Any links that do not use the HTTP/S protocol or are unable to be properly
	 * parsed (throwing an {@link MalformedURLException}) will not be included.
	 *
	 * @param base the base URL used to convert relative links to absolute URLs
	 * @param html the raw HTML associated with the base URL
	 * @param links the data structure to store found HTTP(S) links
	 *
	 * @see Pattern#compile(String)
	 * @see Matcher#find()
	 * @see Matcher#group(int)
	 *
	 * @see #convertUrl(URL, String)
	 * @see #isHttp(URL)
	 */
	public static void findLinks(URL base, String html, Collection<URL> links) {
		Pattern pattern = Pattern.compile("(?s)(?i)(<a\\s+[^>]*?href\\s*=\\s*)(\\\"[^\\\"]+\\\")");
		Matcher matcher = pattern.matcher(html);
		while (matcher.find()) {
			for (int i = 2; i <= (matcher.groupCount()); i++) { // TODO Shouldn't need to loop?
				if (!matcher.group(i).contains("<a")) { // TODO Another linear search
					String link = matcher.group(i).replaceAll("\"", ""); // TODO Unnecessary string operations
					URL newUrl = convertUrl(base, link);
					if ((newUrl != null) && isHttp(newUrl)) {
						links.add(newUrl);
					}
				}
			}
		}
	}

	/**
	 * Determines whether the URL provided uses the HTTP or HTTPS protocol.
	 *
	 * @param url the URL to check
	 * @return true if the URL uses the HTTP or HTTPS protocol
	 */
	public static boolean isHttp(URL url) {
		return (url != null) && url.getProtocol().matches("(?i)https?");
	}

	/**
	 * Returns a list of all the valid HTTP(S) URLs found in the HREF attribute of
	 * the anchor tags in the provided HTML.
	 *
	 * @param base the base URL used to convert relative links to absolute URLs
	 * @param html the raw HTML associated with the base URL
	 * @return list of all valid HTTP(S) links in the order they were found
	 *
	 * @see #findLinks(URL, String, Collection)
	 */
	public static ArrayList<URL> listUrls(URL base, String html) {
		ArrayList<URL> urls = new ArrayList<URL>();
		findLinks(base, html, urls);
		return urls;
	}

	/**
	 * Attempts to create a URI from the provided text, treating the text as a
	 * relative path component if the process initially fails.
	 *
	 * @param link the text value to convert to URI
	 * @return the URI or {@code null} if unable to create
	 *
	 * @see URI#create(String)
	 */
	public static URI makeUri(String link) {
		try {
			return new URI(link);
		} catch (URISyntaxException e) {
			int hash = link.lastIndexOf("#"); // find fragment
			link = hash > 0 ? link.substring(0, hash) : link; // remove fragment
			try {
				return new URI(null, link, null); //convert as relative
			} catch (URISyntaxException f) {
				return null;
			}
		}
	}

	/**
	 * Returns a set of all the unique valid HTTP(S) URLs found in the HREF
	 * attribute of the anchor tags in the provided HTML.
	 *
	 * @param base the base URL used to convert relative URLs to absolute3
	 * @param html the raw HTML associated with the base URL
	 * @return set of all valid and unique HTTP(S) links found
	 *
	 * @see #findLinks(URL, String, Collection)
	 */
	public static HashSet<URL> uniqueUrls(URL base, String html) {
		HashSet<URL> urls = new HashSet<URL>();
		findLinks(base, html, urls);
		return urls;
	}
}
