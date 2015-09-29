package de.webis.nizza.localhistograms;

import java.io.IOException;
import java.util.List;

import corpus.CorpusManager;
import corpus.ICorpusManager;

public class Main {

	public void doStuff() throws IOException {

		String path = "asfd";

		ICorpusManager corpus = new CorpusManager(path);

		String inputText = "Hallo Welt, wir sind\n alle noch da!";
		String text = NGramGenerator.removeLineBreaksAndOtherStuff(inputText);
		List<String> nGrams = NGramGenerator.generateCharNgram(3, text);

		System.out.println(inputText);
		System.out.println(text);
		System.out.println(nGrams);

	}

	public static void main(String[] args) {

		if (args == null || args.length == 0) {
			System.err.println("Please provide correct arguments.");
		} else {
			new Main().doStuff();
		}

	}
}
