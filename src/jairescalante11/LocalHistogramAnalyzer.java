package jairescalante11;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import jairescalante11.aggregatedHistogram.AggregatedHistogram;
import jairescalante11.ngram.NGramGenerator;
import jairescalante11.svm.EnrichedSvmResult;
import jairescalante11.svm.Svm;
import jairescalante11.svm.SvmResult;
import libsvm.svm_model;
import corpus.CorpusManager;
import corpus.TextInstance;
import jairescalante11.aggregatedHistogram.BagOfLocalHistograms;
import jairescalante11.ngram.CharNGramGenerator;

public class LocalHistogramAnalyzer {

	private final long mostCommonNGramCount = 2500;
	private final int numberOfLocalHistograms = 5;
	private final int nGramSize = 3;
	private final String inputPath;
	private final NGramGenerator nGramGenerator = new CharNGramGenerator();
	private final AggregatedHistogram histogramAggregator = new BagOfLocalHistograms();

	public LocalHistogramAnalyzer(String inputPath) {
		this.inputPath = inputPath;
	}

	public List<EnrichedSvmResult> analyze() throws IOException {

		CorpusManager corpus = new CorpusManager(inputPath);

		List<Document> documents = generateNgrams(corpus, nGramSize,
				nGramGenerator);

		List<String> vocabulary = generateVocabulary(documents);

		List<Document> sortedLowbowDocuments = generateLowbowForAllDocs(
				documents, vocabulary, numberOfLocalHistograms);

		// TODO werte in vektor zwischen 0 und 1

		// Files.write(Paths.get("/tmp/run2.txt"),
		// sortedLowbowDocuments.stream()
		// .map(e -> e.toString()).collect(Collectors.toList()));

		List<EnrichedSvmResult> svmResults = predictAuthorsWithSVM(corpus,
				sortedLowbowDocuments);

		// Debug outputs
		System.out.println(svmResults);
		int correct = 0;
		int wrong = 0;
		for (EnrichedSvmResult svmResult : svmResults) {
			if (svmResult.getSvmResult().getActual() == svmResult
					.getSvmResult().getPrediction()) {
				correct++;
			} else {
				wrong++;
			}
		}

		System.out.println("correct:" + correct + " wrong:" + wrong
				+ " percent-correct:" + (double) correct
				/ (double) corpus.getUnknownTextCount());

		return svmResults;
	}

	private int compareEntryToEntry(Entry<String, Long> one,
			Entry<String, Long> two) {

		// first comparision level
		int i = two.getValue().compareTo(one.getValue());
		if (i != 0) {
			return i;
		}

		// second comparison level
		return two.getKey().compareTo(one.getKey());
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
				.sorted(this::compareEntryToEntry).limit(mostCommonNGramCount)
				.map(e -> e.getKey()).collect(Collectors.toList());

		documents.stream().forEach(
				e -> e.setTerms(e.getTerms().stream()
						.filter(d -> vocabularyList.contains(d))
						.collect(Collectors.toList())));
		return vocabularyList;
	}

	private List<Document> generateLowbowForAllDocs(List<Document> documents,
			List<String> vocabulary, int numberOfLocalHistograms) {
		System.out.println("[LOG] generating LOWBOW");
		List<Document> sortedLowbowDocuments = documents
				.parallelStream()
				.map(e -> {// generate set of Histograms
					List<List<Double>> localHistograms = generateLocalHistograms(
							vocabulary, numberOfLocalHistograms,
							e.getNGramCount(), e.getTerms());

					// aggregate histograms to single lowbow histogram
					List<Double> lowbowHist = histogramAggregator
							.generateAggregatedHistogram(vocabulary,
									localHistograms);

					return new Document(e.getTextInstance(), e.getTerms(),
							lowbowHist);
				}).sorted().collect(Collectors.toList());
		return sortedLowbowDocuments;
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

	private List<EnrichedSvmResult> predictAuthorsWithSVM(CorpusManager corpus,
			List<Document> documents) {
		System.out.println("[LOG] Running SVM");

		List<Document> unknown = documents
				.stream()
				.filter(e -> e.getTextInstance().getTrueAuthor()
						.contains("unknown")).collect(Collectors.toList());

		Svm svm = new Svm(documents);
		svm_model model = svm.svmTrain();

		List<EnrichedSvmResult> svmResults = new ArrayList<>();
		for (Document unknownDoc : unknown) {
			List<Double> toPassValues = new ArrayList<>();

			//first element in line (list) is true author of the document - to be predicted by svm
			HashMap<String, String> authorTextMapping = corpus
					.getAuthorTextMapping();
			if (authorTextMapping != null) {
				toPassValues.add(Double.parseDouble(authorTextMapping.get(
						unknownDoc.getTextInstance().getTextSource().getName())
						.substring(9, 14)));
			} else {
				//No ground truth file available
				toPassValues.add(Double.NaN);
			}
			
			toPassValues.addAll(unknownDoc.getLowbowHistogram());
			SvmResult result = svm.evaluate(toPassValues
					.toArray(new Double[unknown.get(0).getLowbowHistogram()
							.size()]), model, corpus.getAllAuthors().size());
			String candidateAuthor = corpus.getAllAuthors().get(
					((int) result.getPrediction()) - 1);
			String file = unknownDoc.getTextInstance().getTextSource()
					.getName();
			svmResults
					.add(new EnrichedSvmResult(candidateAuthor, file, result));
		}
		return svmResults;
	}

	private List<Document> generateNgrams(CorpusManager corpus, int nGramSize,
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

		List<String> nGrams = nGramtype.generateNgrams(nGramSize, docText);

		Document doc = new Document(singleText);
		doc.setTerms(nGrams);

		return doc;
	}

}
