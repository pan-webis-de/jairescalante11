package de.webis.nizza.localhistograms;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import corpus.TextInstance;

public class Document {

	private TextInstance text;
	private List<String> termsW;

	public Document(TextInstance text) {
		super();
		this.text = text;
	}

	public List<String> getTerms() {
		return termsW;
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
}
