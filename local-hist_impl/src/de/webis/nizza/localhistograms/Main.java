package de.webis.nizza.localhistograms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import libsvm.svm_model;
import corpus.CorpusManager;
import corpus.ICorpusManager;
import corpus.TextInstance;
import de.webis.nizza.localhistograms.ngram.CharNGramGenerator;
import de.webis.nizza.localhistograms.ngram.NGramGenerator;
import de.webis.nizza.localhistograms.svm.Svm;
import de.webis.nizza.localhistograms.svm.SvmResult;

public class Main {

	public void doStuff() throws IOException {

		List<Integer> numberOfHistograms = Stream.of(2, 5, 20).collect(
				Collectors.toList());

		String path = "../corpora/C10/"; // TODO variable
		CorpusManager corpus = new CorpusManager(path);

		List<Document> documents = generateNgrams(corpus, 3,
				new CharNGramGenerator());

		List<String> vocabulary = generateVocabulary(documents);

		int numberOfLocalHistograms = 5; // TODO from list! -> foreach

		// TODO start numberOfHistograms foreach
		generateLowbowForAllDocs(documents, vocabulary, numberOfLocalHistograms);

		// TODO nur erste 2500
		// TODO werte in vektor zwischen 0 und 1

		List<SvmResult> svmResults = predictAuthorsWithSVM(corpus, documents);
		// TODO end numberOfHistograms foreach

		System.out.println(svmResults);

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

	private List<String> generateVocabulary(List<Document> documents) {
		System.out.println("[LOG] starting vocabulary");
		Map<String, Long> vocabulary = documents
				.parallelStream()
				.flatMap(e -> e.getTerms().stream())
				.collect(
						Collectors.groupingBy(Function.identity(),
								Collectors.counting()));

		System.out.println("[LOG] Finished Vocabulary");
		List<String> vocabularyList = vocabulary.keySet().stream()
				.collect(Collectors.toList());
		return vocabularyList;
	}

	private void generateLowbowForAllDocs(List<Document> documents,
			List<String> vocabulary, int numberOfLocalHistograms) {
		ExecutorService exec = Executors.newCachedThreadPool();

		for (Document document : documents) {

			Future<List<Double>> lowbowHistResult = exec
					.submit(new LocalHistogramGenerator(document
							.getNGramCount(), document.getTerms(),
							numberOfLocalHistograms, vocabulary));
			document.setLowbowHistogram(lowbowHistResult);

		}
	}

	private List<SvmResult> predictAuthorsWithSVM(CorpusManager corpus,
			List<Document> documents) {
		System.out.println("[LOG] Running SVM");

		List<Document> unknown = documents
				.stream()
				.filter(e -> e.getTextInstance().getTrueAuthor()
						.contains("unknown")).collect(Collectors.toList());

		Svm svm = new Svm(documents);
		svm_model model = svm.svmTrain();

		List<SvmResult> svmResults = new ArrayList<>();
		for (Document unknownDoc : unknown) {
			List<Double> toPassValues = new ArrayList<>();
			toPassValues.add(Double
					.parseDouble(corpus
							.getAuthorTextMapping()
							.get(unknownDoc.getTextInstance().getTextSource()
									.getName()).substring(9, 14))); // TODO
																	// 7,12?
			toPassValues.addAll(unknownDoc.getLowbowHistogram());
			SvmResult result = svm.evaluate(toPassValues
					.toArray(new Double[unknown.get(0).getLowbowHistogram()
							.size()]), model, corpus.getAllAuthors().size());
			svmResults.add(result);
		}
		return svmResults;
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
