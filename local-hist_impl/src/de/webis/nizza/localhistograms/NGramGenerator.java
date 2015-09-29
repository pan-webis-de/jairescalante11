package de.webis.nizza.localhistograms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NGramGenerator {

	public static List<String> generateCharNgram(int nGramSize, String text) {

		List<String> nGramResult = new LinkedList<>();

		for (int i = 0; i < text.length() - nGramSize + 1; i++) {
			nGramResult.add(text.substring(i, i + nGramSize));
		}

		return nGramResult;
	}

	public static List<String> generateWordNgrams(int n, String str) {
		List<String> ngrams = new ArrayList<String>();
		String[] words = str.split(" ");
		for (int i = 0; i < words.length - n + 1; i++)
			ngrams.add(concat(words, i, i + n));
		return ngrams;
	}

	private static String concat(String[] words, int start, int end) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++)
			sb.append((i > start ? " " : "") + words[i]);
		return sb.toString();
	}

	public static String removeLineBreaksAndOtherStuff(String string) {
		return string.replaceAll("\\s+", " ");

	}
}
