package de.webis.nizza.localhistograms;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import corpus.CorpusManager;
import corpus.ICorpusManager;
import corpus.TextInstance;

public class Main {

	public void doStuff() throws IOException {

		String path = "asfd";
		ICorpusManager corpus = new CorpusManager(path);

		List<Document> documents = new LinkedList<Document>();

		TextInstance singleText = corpus.getNextText();
		while (singleText != null) {

			String docText = NGramGenerator
					.removeLineBreaksAndOtherStuff(singleText.getFullText());
			// TODO ngrams variabel
			List<String> nGrams = NGramGenerator.generateCharNgram(3, docText);

			Document doc = new Document(singleText);
			doc.setTerms(nGrams);

			System.out.println(doc.generateFrequencyMap());

			documents.add(doc);

			singleText = corpus.getNextText();
		}

		// String inputText = "Hallo Welt, wir sind\n alle noch da!";
		// String text =
		// NGramGenerator.removeLineBreaksAndOtherStuff(inputText);
		// List<String> nGrams = NGramGenerator.generateCharNgram(3, text);
		//
		// System.out.println(inputText);
		// System.out.println(text);
		// System.out.println(nGrams);

	}

	public static void main(String[] args) {

		if (args == null || args.length == 0) {
			System.err.println("Please provide correct arguments.");
		} else {
			try {
				new Main().doStuff();
			} catch (IOException e) {
				System.err.println("Fail. " + e.getMessage());
			}
		}

	}
}
