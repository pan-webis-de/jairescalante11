package jairescalante11;

import jairescalante11.aggregatedHistogram.AggregatedHistogram;
import jairescalante11.aggregatedHistogram.BagOfLocalHistograms;
import jairescalante11.ngram.CharNGramGenerator;
import jairescalante11.ngram.NGramGenerator;
import jairescalante11.svm.EnrichedSvmResult;
import jairescalante11.svm.SvmCalc;
import jairescalante11.svm.SvmResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import libsvm.svm_model;
import corpus.CorpusManager;
import corpus.TextInstance;

/**
 * Central class of Implementation. Start with .analyze()
 *
 * @author Michael Träger
 */
public class LocalHistogramAnalyzer {

    private final long mostCommonNGramCount;
    private final int numberOfLocalHistograms;
    private final int nGramSize;
    private final String inputPath;
    private final NGramGenerator nGramGenerator;
    private final AggregatedHistogram histogramAggregator;

    /**
     * Create analyzer. This constructor uses default values showing good results as presented in the paper.
     *
     * @param inputPath directory where to read the input files
     */
    public LocalHistogramAnalyzer(String inputPath) {
        this.inputPath = inputPath;
        this.mostCommonNGramCount = 2500;
        this.numberOfLocalHistograms = 5;
        this.nGramSize = 3;
        this.nGramGenerator = new CharNGramGenerator();
        this.histogramAggregator = new BagOfLocalHistograms();
    }


    /**
     * Create analyzer. Provide specific values and implementations.
     *
     * @param inputPath               directory where to read the input files
     * @param mostCommonNGramCount    select nGram limitation (default: 2500)
     * @param numberOfLocalHistograms select number of created local histograms (default: 5)
     * @param nGramSize               select size of nGrams (default: 3)
     * @param nGramGenerator          select type of nGrams. Use implementations of NGramGenerator: CharNGramGenerator or WordNGramGenerator
     *                                (default: CharNGramGenerator)
     * @param histogramAggregator     select aggregation method for histograms.  Use implementations of AggregatedHistogram: BagOfLocalHistograms or LowbowHistogram
     *                                (default: BagOfLocalHistograms)
     */
    public LocalHistogramAnalyzer(String inputPath, long mostCommonNGramCount, int numberOfLocalHistograms, int nGramSize,
                                  NGramGenerator nGramGenerator, AggregatedHistogram histogramAggregator) {
        this.inputPath = inputPath;
        this.mostCommonNGramCount = mostCommonNGramCount;
        this.numberOfLocalHistograms = numberOfLocalHistograms;
        this.nGramSize = nGramSize;
        this.nGramGenerator = nGramGenerator;
        this.histogramAggregator = histogramAggregator;
    }


    /**
     * Start analyzing.
     *
     * @return the result from SVM
     * @throws IOException
     */
    public List<EnrichedSvmResult> analyze() throws IOException {

        CorpusManager corpus = new CorpusManager(inputPath);

        List<Document> documents = generateNgrams(corpus, nGramSize, nGramGenerator);

        List<String> vocabulary = generateVocabulary(documents);

        List<Document> sortedLowbowDocuments = generateLowbowForAllDocs(documents, vocabulary, numberOfLocalHistograms);

        // optional optimization: values in vector between 0 and 1

        List<EnrichedSvmResult> svmResults = predictAuthorsWithSVM(corpus, sortedLowbowDocuments);

        // Debug outputs for quick analyzing
        debugOutputs(corpus, svmResults);

        return svmResults;
    }

    /**
     * Print result of this run to console - including percentage of correct results
     *
     * @param corpus
     * @param svmResults
     */
    private void debugOutputs(CorpusManager corpus, List<EnrichedSvmResult> svmResults) {
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
    }

    /**
     * compare entries to each other on two levels for correct sorting
     *
     * @param one
     * @param two
     * @return
     */
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
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
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

    private List<Document> generateLowbowForAllDocs(List<Document> documents, List<String> vocabulary, int numberOfLocalHistograms) {
        System.out.println("[LOG] generating LOWBOW");
        List<Document> sortedLowbowDocuments = documents
                .parallelStream()
                .map(e -> {// generate set of Histograms
                    List<List<Double>> localHistograms = generateLocalHistograms(
                            vocabulary, numberOfLocalHistograms, e.getNGramCount(), e.getTerms());

                    // aggregate histograms to single lowbow histogram
                    List<Double> lowbowHist = histogramAggregator
                            .generateAggregatedHistogram(vocabulary, localHistograms);

                    return new Document(e.getTextInstance(), e.getTerms(), lowbowHist);
                }).sorted().collect(Collectors.toList());
        return sortedLowbowDocuments;
    }

    private List<List<Double>> generateLocalHistograms(List<String> vocabulary, int numberOfLocalHistograms, int numberOfNgrams, List<String> terms) {
        List<List<Double>> localHistograms = new LinkedList<>();
        for (int i = 0; i < numberOfLocalHistograms; i++) {
            List<Double> localHist = new ArrayList<>();
            double mu = (double) i / (double) numberOfLocalHistograms;
            for (int j = 0; j < vocabulary.size(); j++) {
                localHist.add(0.d);
            }
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

    private List<EnrichedSvmResult> predictAuthorsWithSVM(CorpusManager corpus, List<Document> documents) {
        System.out.println("[LOG] Running SVM");

        //only unknown documents here
        List<Document> unknown = documents
                .stream()
                .filter(e -> e.getTextInstance().getTrueAuthor()
                        .contains("unknown")).collect(Collectors.toList());

        SvmCalc svm = new SvmCalc(documents);
        svm_model model = svm.trainSvm();

        List<EnrichedSvmResult> svmResults = new ArrayList<>();
        for (Document unknownDoc : unknown) {
            List<Double> toPassValues = new ArrayList<>();

            //first element in line (list) is true author of the document - to be predicted by svm
            HashMap<String, String> authorTextMapping = corpus.getAuthorTextMapping();
            if (authorTextMapping != null) {
                toPassValues.add(Double.parseDouble(authorTextMapping.get(
                        unknownDoc.getTextInstance().getTextSource().getName())
                        .substring(9, 14)));
                //.substring required for naming conventions - specific to the test files
            } else {
                //No ground truth file available
                toPassValues.add(Double.NaN);
            }

            toPassValues.addAll(unknownDoc.getLowbowHistogram());
            SvmResult result = svm.evaluate(toPassValues
                    .toArray(new Double[unknown.get(0).getLowbowHistogram().size()]), model, corpus.getAllAuthors().size());
            String candidateAuthor = corpus.getAllAuthors().get(((int) result.getPrediction()) - 1);
            String file = unknownDoc.getTextInstance().getTextSource().getName();
            svmResults.add(new EnrichedSvmResult(candidateAuthor, file, result));
        }
        return svmResults;
    }

    private List<Document> generateNgrams(CorpusManager corpus, int nGramSize, NGramGenerator nGramtype) throws IOException {
        List<Document> documents = new LinkedList<Document>();

        // For known texts
        TextInstance singleText = corpus.getNextText();
        while (singleText != null) {
            documents.add(generateDocumentStuff(nGramSize, nGramtype, singleText));
            singleText = corpus.getNextText();
        }

        // For unknown texts
        singleText = corpus.getUnknownText();
        while (singleText != null) {
            documents.add(generateDocumentStuff(nGramSize, nGramtype, singleText));
            singleText = corpus.getUnknownText();
        }

        return documents;
    }

    private Document generateDocumentStuff(int nGramSize, NGramGenerator nGramtype, TextInstance singleText) throws IOException {
        String docText = NGramGenerator.removeLineBreaksAndOtherStuff(singleText.getFullText());

        List<String> nGrams = nGramtype.generateNgrams(nGramSize, docText);

        Document doc = new Document(singleText);
        doc.setTerms(nGrams);

        return doc;
    }

}
