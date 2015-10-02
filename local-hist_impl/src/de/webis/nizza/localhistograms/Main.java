package de.webis.nizza.localhistograms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import libsvm.svm_model;
import corpus.CorpusManager;
import corpus.ICorpusManager;
import corpus.TextInstance;
import de.webis.nizza.localhistograms.ngram.CharNGramGenerator;
import de.webis.nizza.localhistograms.ngram.NGramGenerator;
import de.webis.nizza.localhistograms.svm.Svm;

public class Main {

	public void doStuff() throws IOException {

		List<Integer> numberOfHistograms = Stream.of(2, 5, 20).collect(
				Collectors.toList());

		String path = "../corpora/test/"; // TODO variable
		ICorpusManager corpus = new CorpusManager(path);

		List<Document> documents = generateNgrams(corpus, 3,
				new CharNGramGenerator());

		List<String> vocabulary = new LinkedList<>();
		for (Document document : documents) {
			for (String singleTerm : document.getTerms()) {
				if (!vocabulary.contains(singleTerm)) {
					vocabulary.add(singleTerm);
				}
			}
		}

		int numberOfLocalHistograms = 5; // TODO from list! -> foreach
		for (Document document : documents) {

			int numberOfNgrams = document.getNGramCount();
			WeightedTerm[][] weightedTerms = generateWeightedTerms(
					numberOfLocalHistograms, document, numberOfNgrams);

			List<List<Double>> localHistograms = generateLocalHistograms(
					vocabulary, numberOfLocalHistograms, numberOfNgrams,
					weightedTerms);

			List<Double> lowbowHistogram = new LinkedList<>();
			for (int i = 0; i < vocabulary.size(); i++) {
				double sum = 0;
				for (int j = 0; j < localHistograms.size(); j++) {
					sum += localHistograms.get(j).get(i);
				}
				lowbowHistogram.add(sum);
			}

			document.setLowbowHistogram(lowbowHistogram);
			// System.out.println(lowbowHistogram);
			System.out.println("[LOG] new LowbowHist generated");
		}

		// TODO nur erste 2500
		// TODO werte in vektor zwischen 0 und 1

		System.out.println("[LOG] Running SVM");

		List<Document> unknown = documents
				.stream()
				.filter(e -> e.getTextInstance().getTrueAuthor()
						.equals("UNKNOWN")).collect(Collectors.toList());

		Svm svm = new Svm(documents);
		svm_model model = svm.svmTrain();

		for (Document unknownDoc : unknown) {
			List<Double> toPassValues = new ArrayList<>();
			toPassValues.add(Double.NaN);
			toPassValues.addAll(unknownDoc.getLowbowHistogram());
			svm.evaluate(
					toPassValues.toArray(new Double[unknown.get(0)
							.getLowbowHistogram().size()]), model);

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

	private List<List<Double>> generateLocalHistograms(List<String> vocabulary,
			int numberOfLocalHistograms, int numberOfNgrams,
			WeightedTerm[][] weightedTerms) {
		List<List<Double>> localHistograms = new LinkedList<>();
		for (int i = 0; i < numberOfLocalHistograms; i++) {
			List<Double> localHist = new ArrayList<>();
			for (int j = 0; j < vocabulary.size(); j++) {
				localHist.add(0.d);
			}
			// List<Double> localHist = Arrays.asList(new
			// Double[vocabulary.size()]);
			for (int j = 0; j < numberOfNgrams; j++) {
				int position = vocabulary
						.indexOf(weightedTerms[i][j].getTerm());

				localHist.set(position, localHist.get(position)
						+ weightedTerms[i][j].getWeight());
			}
			localHistograms.add(localHist);
		}
		return localHistograms;
	}

	private WeightedTerm[][] generateWeightedTerms(int numberOfLocalHistograms,
			Document document, int numberOfNgrams) {
		WeightedTerm[][] weightedTerms = new WeightedTerm[numberOfLocalHistograms][numberOfNgrams];
		// TODO check this stuff
		for (int i = 0; i < numberOfLocalHistograms; i++) {
			double mu = (double) i / (double) numberOfLocalHistograms;
			for (int j = 0; j < numberOfNgrams; j++) {
				double position = (double) j / (double) numberOfNgrams;
				double weight = Document.kernelFunction(mu, position);
				weightedTerms[i][j] = new WeightedTerm(document.getTerms().get(
						j), weight);
			}
		}
		return weightedTerms;
	}

	private List<Document> generateNgrams(ICorpusManager corpus, int nGramSize,
			NGramGenerator nGramtype) throws FileNotFoundException, IOException {
		List<Document> documents = new LinkedList<Document>();

		// For known texts
		TextInstance singleText = corpus.getNextText();
		while (singleText != null) {

			documents.add(generateDocumentStuff(nGramSize, nGramtype,
					singleText));

			singleText = corpus.getNextText();
		}

		// For unknown texts
		singleText = corpus.getUnknownText();
		while (singleText != null) {

			documents.add(generateDocumentStuff(nGramSize, nGramtype,
					singleText));

			singleText = corpus.getUnknownText();
		}

		return documents;
	}

	private Document generateDocumentStuff(int nGramSize,
			NGramGenerator nGramtype, TextInstance singleText)
			throws FileNotFoundException, IOException {
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
		return doc;
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
