package jairescalante11.ngram;

import java.util.List;

public interface NGramGenerator {

	public List<String> generateNgrams(int nGramSize, String text);

	public static String removeLineBreaksAndOtherStuff(String string) {
		return string.replaceAll("\\s+", " ");

	}

}
