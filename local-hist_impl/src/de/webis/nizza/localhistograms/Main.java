package de.webis.nizza.localhistograms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.distribution.NormalDistribution;

import corpus.CorpusManager;
import corpus.ICorpusManager;
import corpus.TextInstance;
import de.webis.nizza.localhistograms.ngram.CharNGramGenerator;
import de.webis.nizza.localhistograms.ngram.NGramGenerator;

public class Main {

	public void doStuff() throws IOException {

		List<Integer> numberOfHistograms = Stream.of(2, 5, 20).collect(
				Collectors.toList());

		String path = "../corpora/test/"; // TODO variable
		ICorpusManager corpus = new CorpusManager(path);

		List<Document> documents = generateNgrams(corpus, 3,
				new CharNGramGenerator());

		int numberOfLocalHistograms = 5; // TODO from list!
		for (Document document : documents) {
			int numberOfNgrams = document.getNGramCount();
			int subsetLength = numberOfNgrams / numberOfLocalHistograms; // floor
			// int muPosition = 0;
			// int singleNGramNumber = 0;
			// while (// numberOfNgrams > termLength * mu &&
			// termLength * (muPosition + 1) < numberOfNgrams) {
			//
			// muPosition++;
			// }
			// // TODO last run until numberOfNgrams out of while loop

			List<Double> termsWeighted = new LinkedList<>();
			for (int i = 0; i < document.getTerms().size(); i++) {
				String term = document.getTerms().get(i);

				termsWeighted.add(kernelFunction(
						Math.ceil((double) i / (double) subsetLength)
								/ (double) numberOfLocalHistograms, (double) i
								/ (double) numberOfNgrams));
			}

		}

		int mu = 5; // TODO check
		double position = 0.1;
		double result = kernelFunction(mu, position);

		// TODO vocabulary
		// Map<String, Long> vocabulary = new HashMap<>();
		// for (Document document : documents) {
		// vocabulary.forEach((k, v) -> document.generateFrequencyMap().merge(
		// k, v, (v1, v2) -> {
		// return Long.sum(v1, v2);
		// }));
		// }
		//
		// System.out.println(vocabulary);

		// String inputText = "Hallo Welt, wir sind\n alle noch da!";
		// String text =
		// NGramGenerator.removeLineBreaksAndOtherStuff(inputText);
		// List<String> nGrams = NGramGenerator.generateCharNgram(3, text);
		//
		// System.out.println(inputText);
		// System.out.println(text);
		// System.out.println(nGrams);

	}

	private double kernelFunction(double mu, double position) {
		double sigma = 0.2;
		double normalDist = new NormalDistribution(mu, sigma)
				.cumulativeProbability(position);
		double standardDist = new NormalDistribution()
				.cumulativeProbability((1 - mu) / sigma)
				- new NormalDistribution().cumulativeProbability((0 - mu)
						/ sigma);
		// TODO nenner immer nicht 0
		double result = normalDist / standardDist;
		return result;
	}

	private List<Document> generateNgrams(ICorpusManager corpus, int nGramSize,
			NGramGenerator nGramtype) throws FileNotFoundException, IOException {
		List<Document> documents = new LinkedList<Document>();

		TextInstance singleText = corpus.getNextText();
		while (singleText != null) {

			String docText = NGramGenerator
					.removeLineBreaksAndOtherStuff(singleText.getFullText());
			// TODO ngrams variabel
			List<String> nGrams = nGramtype.generateNgrams(nGramSize, docText);

			Document doc = new Document(singleText);
			doc.setTerms(nGrams);

			// TODO map to set?
			// System.out.println(doc.generateFrequencyMap());
			// Set<Entry<String, Long>> entries = doc.generateFrequencyMap()
			// .entrySet();
			// System.out.println(entries);

			documents.add(doc);

			singleText = corpus.getNextText();
		}
		return documents;
	}

	public static void main(String[] args) throws IOException {

		if (args == null || args.length == 0) {
			System.err.println("Please provide correct arguments.");
		} else {
			try {
				new Main().doStuff();
			} catch (IOException up) {
				System.err.println("Fail. " + up.getMessage());
				throw up;
			}
		}

	}
}
