package edu.stanford.cs276;

import java.util.List;
import java.util.Map;

/**
 * The class used to store useful information for a document. You can also write the document to a string for debugging.
 */
public class Document {

	public String url = null;
	public String title = null;
	public List<String> headers = null;
	public Map<String, List<Integer>> body_hits = null; // term -> [list of positions]
	public int body_length = 0;
	public int page_rank = 0;
	public Map<String, Integer> anchors = null; // term -> anchor_count

	public Document(String url) {
		this.url=url;
	}
	
	// For debug
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		if (title != null) result.append("title: " + title + NEW_LINE);
		if (headers != null) result.append("headers: " + headers.toString() + NEW_LINE);
		if (body_hits != null) result.append("body_hits: " + body_hits.toString() + NEW_LINE);
		if (body_length != 0) result.append("body_length: " + body_length + NEW_LINE);
		if (page_rank != 0) result.append("page_rank: " + page_rank + NEW_LINE);
		if (anchors != null) result.append("anchors: " + anchors.toString() + NEW_LINE);
		return result.toString();
	}
}
