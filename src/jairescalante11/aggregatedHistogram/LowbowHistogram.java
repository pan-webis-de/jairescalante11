package jairescalante11.aggregatedHistogram;

import java.util.LinkedList;
import java.util.List;

public class LowbowHistogram implements AggregatedHistogram {

	@Override
	public List<Double> generateAggregatedHistogram(List<String> vocabulary,
			List<List<Double>> localHistograms) {
		List<Double> lowbowHist = new LinkedList<>();
		for (int i = 0; i < vocabulary.size(); i++) {
			double sum = 0;
			for (int j = 0; j < localHistograms.size(); j++) {
				sum += localHistograms.get(j).get(i);
			}
			lowbowHist.add(sum);
		}
		return lowbowHist;

	}

}
