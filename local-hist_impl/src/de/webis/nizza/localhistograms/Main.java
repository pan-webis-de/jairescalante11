package de.webis.nizza.localhistograms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

		int numberOfLocalHistograms = 5; // TODO from list! -> foreach
		for (Document document : documents) {

			// TODO needed? makes sense? wtf?
			List<Double> posWeighted = document
					.getTermsPositionWeighted(numberOfLocalHistograms);

			// one elem for every kernel location, each of this elems contains a
			// list over all
			List<List<WeightedTermElement>> kernelLocationTermsList = new LinkedList<>();

			int numberOfNgrams = document.getNGramCount();
			int subsetLength = numberOfNgrams / numberOfLocalHistograms; // floor
			int muPosition = 0;
			while (// numberOfNgrams > subsetLength * mu &&
			subsetLength * (muPosition + 1) < numberOfNgrams) {
				List<WeightedTermElement> weightedTerms = new LinkedList<>();
				for (int i = muPosition * subsetLength; i < muPosition
						+ subsetLength; i++) {
					Double localWeightForMuAtTheMoment = Document
							.kernelFunction(muPosition + 1, 0.2);
					String localNgram = document.getTerms().get(i);
					weightedTerms.add(new WeightedTermElement(
							localWeightForMuAtTheMoment, i, localNgram,
							muPosition + 1));
				}
				kernelLocationTermsList.add(weightedTerms);
				muPosition++;
			}
			// TODO last run until numberOfNgrams out of while loop
			List<WeightedTermElement> weightedTerms = new LinkedList<>();
			for (int i = muPosition * subsetLength; i < numberOfNgrams; i++) {
				Double localWeightForMuAtTheMoment = Document.kernelFunction(
						muPosition + 1, 0.2);
				String localNgram = document.getTerms().get(i);
				weightedTerms.add(new WeightedTermElement(
						localWeightForMuAtTheMoment, i, localNgram,
						muPosition + 1));
			}
			kernelLocationTermsList.add(weightedTerms);

			// TODO split in seperate parts -> LH?

			// generate Hist for each Kernel location

			//
			for (List<WeightedTermElement> kernelLocation : kernelLocationTermsList) {
				Map<WeightedTermElement, Long> collect = kernelLocation
						.stream().collect(
								Collectors.groupingBy(Function.identity(),
										Collectors.counting()));
				System.out.println(collect);
			}

		}

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
