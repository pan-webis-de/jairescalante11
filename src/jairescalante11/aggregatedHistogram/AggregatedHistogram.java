package jairescalante11.aggregatedHistogram;

import java.util.List;

public interface AggregatedHistogram {

	List<Double> generateAggregatedHistogram(List<String> vocabulary,
			List<List<Double>> localHistograms);
}
