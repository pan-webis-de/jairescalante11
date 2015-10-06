package de.webis.nizza.localhistograms.aggregatedHistogram;

import java.util.List;

public interface AggregatedHistogram {

	List<Double> generateAggregatedHistogram(List<String> vocabulary,
			List<List<Double>> localHistograms);
}
