package de.webis.nizza.localhistograms.ngram;

import java.util.LinkedList;
import java.util.List;

public class CharNGramGenerator implements NGramGenerator {

	public List<String> generateNgrams(int nGramSize, String text) {

		List<String> nGramResult = new LinkedList<>();

		for (int i = 0; i < text.length() - nGramSize + 1; i++) {
			nGramResult.add(text.substring(i, i + nGramSize));
		}

		return nGramResult;
	}

}
