package jairescalante11.aggregatedHistogram;

import java.util.LinkedList;
import java.util.List;

public class BagOfLocalHistograms implements AggregatedHistogram {

	private int singleHistogramSize;

	@Override
	public List<Double> generateAggregatedHistogram(List<String> vocabulary,
			List<List<Double>> localHistograms) {
		List<Double> bolh = new LinkedList<>();

		for (List<Double> singleHistogram : localHistograms) {
			bolh.addAll(singleHistogram);
		}

		singleHistogramSize = localHistograms.get(0).size();

		return bolh;
	}

	public int getSingleHistogramSize() {
		return singleHistogramSize;
	}

}
