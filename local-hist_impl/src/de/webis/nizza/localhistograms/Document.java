package de.webis.nizza.localhistograms;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.NormalDistribution;

import corpus.TextInstance;

public class Document implements Comparable<Document> {

	private TextInstance text;
	private List<String> termsW;
	private List<Double> lowbowHistogram;

	public Document(TextInstance text) {
		super();
		this.text = text;
	}

	public Document(TextInstance text, List<String> termsW,
			List<Double> lowbowHistogram) {
		super();
		this.text = text;
		this.termsW = termsW;
		this.lowbowHistogram = lowbowHistogram;
	}

	public List<String> getTerms() {
		return termsW;
	}

	public int getNGramCount() {
		return termsW.size();
	}

	public void setTerms(List<String> termsw) {
		this.termsW = termsw;

	}

	public TextInstance getTextInstance() {
		return text;
	}

	public Map<String, Long> generateFrequencyMap() {
		if (termsW == null) {
			throw new IllegalStateException("set terms first");
		}

		// return termsW.stream().collect(
		// Collectors.toMap(e -> e, Function.identity()));

		Map<String, Long> collect = termsW.stream().collect(
				Collectors.groupingBy(Function.identity(),
						Collectors.counting()));

		return collect;

	}

	public List<Double> getTermsPositionWeighted(int numberOfLocalHistograms) {
		int numberOfNgrams = this.getNGramCount();
		int subsetLength = numberOfNgrams / numberOfLocalHistograms; // floor
		List<Double> termsWeighted = new LinkedList<>();
		for (int i = 0; i < this.getTerms().size(); i++) {
			String term = this.getTerms().get(i);

			termsWeighted.add(kernelFunction(
					Math.ceil((double) i / (double) subsetLength)
							/ (double) numberOfLocalHistograms, (double) i
							/ (double) numberOfNgrams));
		}

		return termsWeighted;
	}

	public static double kernelFunction(double mu, double position) {
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

	// FIXME test!
	public static Map<String, Long> mergeMaps(Map<String, Long> bigOne,
			Map<String, Long> map2) {
		bigOne.forEach((k, v) -> map2.merge(k, v, (v1, v2) -> {
			return Long.sum(v1, v2);
		}));
		return bigOne;
	}

	public List<Double> getLowbowHistogram() {
		return lowbowHistogram;
	}

	public void setLowbowHistogram(List<Double> lowbowHistogram) {
		this.lowbowHistogram = lowbowHistogram;
	}

	@Override
	public int compareTo(Document o) {
		// Comparing by FileNames
		return text.getTextSource().getName()
				.compareTo(o.getTextInstance().getTextSource().getName());
	}

}
