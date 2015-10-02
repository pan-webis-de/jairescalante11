package de.webis.nizza.localhistograms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class LocalHistogramGenerator implements Callable<List<Double>> {

	private int numberOfLocalHistograms;
	private List<String> vocabulary;
	private int numberOfNgrams;
	private List<String> terms;

	public LocalHistogramGenerator(int numberOfNgrams, List<String> terms,
			int numberOfLocalHistograms, List<String> vocabulary) {
		this.numberOfNgrams = numberOfNgrams;
		this.numberOfLocalHistograms = numberOfLocalHistograms;
		this.vocabulary = vocabulary;
		this.terms = terms;
	}

	@Override
	public List<Double> call() throws Exception {
		WeightedTerm[][] weightedTerms = generateWeightedTerms(
				numberOfLocalHistograms, terms, numberOfNgrams);

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

		// document.setLowbowHistogram(lowbowHistogram);
		// System.out.println(lowbowHistogram);
		System.out.println("[LOG] new LowbowHist generated");
		return lowbowHistogram;
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
			List<String> terms, int numberOfNgrams) {
		WeightedTerm[][] weightedTerms = new WeightedTerm[numberOfLocalHistograms][numberOfNgrams];
		// TODO check this stuff
		for (int i = 0; i < numberOfLocalHistograms; i++) {
			double mu = (double) i / (double) numberOfLocalHistograms;
			for (int j = 0; j < numberOfNgrams; j++) {
				double position = (double) j / (double) numberOfNgrams;
				double weight = Document.kernelFunction(mu, position);
				weightedTerms[i][j] = new WeightedTerm(terms.get(j), weight);
			}
		}
		return weightedTerms;
	}

}
