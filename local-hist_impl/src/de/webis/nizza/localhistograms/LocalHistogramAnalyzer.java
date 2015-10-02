package de.webis.nizza.localhistograms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import libsvm.svm_model;
import corpus.CorpusManager;
import corpus.ICorpusManager;
import corpus.TextInstance;
import de.webis.nizza.localhistograms.ngram.CharNGramGenerator;
import de.webis.nizza.localhistograms.ngram.NGramGenerator;
import de.webis.nizza.localhistograms.svm.Svm;
import de.webis.nizza.localhistograms.svm.SvmResult;

public class LocalHistogramAnalyzer {

	private final long mostCommonNGramCount = 2500;
	private final int numberOfLocalHistograms = 5;
	private final int nGramSize = 3;
	private String outputPath;
	private String inputPath;

	public LocalHistogramAnalyzer(String inputPath, String outputPath) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
	}

	public void analyze() throws IOException {

		CorpusManager corpus = new CorpusManager(inputPath);

		List<Document> documents = generateNgrams(corpus, nGramSize,
				new CharNGramGenerator());

		List<String> vocabulary = generateVocabulary(documents);

		generateLowbowForAllDocs(documents, vocabulary, numberOfLocalHistograms);

		// TODO werte in vektor zwischen 0 und 1

		List<SvmResult> svmResults = predictAuthorsWithSVM(corpus, documents);

		System.out.println(svmResults);

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
		List<String> vocabularyList = vocabulary.entrySet().stream()
				.sorted((a, b) -> b.getValue().compareTo(a.getValue()))
				.limit(mostCommonNGramCount).map(e -> e.getKey())
				.collect(Collectors.toList());
		// List<String> vocabularyList = vocabulary.keySet().stream()
		// .collect(Collectors.toList());
		documents.stream().forEach(
				e -> e.setTerms(e.getTerms().stream()
						.filter(d -> vocabularyList.contains(d))
						.collect(Collectors.toList())));
		return vocabularyList;
	}

	private void generateLowbowForAllDocs(List<Document> documents,
			List<String> vocabulary, int numberOfLocalHistograms) {
		documents
				.parallelStream()
				.map(e -> {// generate set of Histograms
					List<List<Double>> localHistograms = generateLocalHistograms(
							vocabulary, numberOfLocalHistograms,
							e.getNGramCount(), e.getTerms());

					// aggregate histograms to single lowbow histogram
					List<Double> lowbowHist = new LinkedList<>();
					for (int i = 0; i < vocabulary.size(); i++) {
						double sum = 0;
						for (int j = 0; j < localHistograms.size(); j++) {
							sum += localHistograms.get(j).get(i);
						}
						lowbowHist.add(sum);
					}
					e.setLowbowHistogram(lowbowHist);
					return lowbowHist;

				}).forEach(e -> System.out.println("[LOG] Lowbow generated"));

	}

	private List<List<Double>> generateLocalHistograms(List<String> vocabulary,
			int numberOfLocalHistograms, int numberOfNgrams, List<String> terms) {
		List<List<Double>> localHistograms = new LinkedList<>();
		for (int i = 0; i < numberOfLocalHistograms; i++) {
			List<Double> localHist = new ArrayList<>();
			double mu = (double) i / (double) numberOfLocalHistograms;
			for (int j = 0; j < vocabulary.size(); j++) {
				localHist.add(0.d);
			}
			// List<Double> localHist = Arrays.asList(new
			// Double[vocabulary.size()]);
			for (int j = 0; j < numberOfNgrams; j++) {
				double position = (double) j / (double) numberOfNgrams;
				double weight = Document.kernelFunction(mu, position);
				int vposition = vocabulary.indexOf(terms.get(j));

				localHist.set(vposition, localHist.get(vposition) + weight);
			}

			localHistograms.add(localHist);
		}
		return localHistograms;
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

}
