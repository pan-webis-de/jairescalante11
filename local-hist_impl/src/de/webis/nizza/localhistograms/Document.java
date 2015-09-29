package de.webis.nizza.localhistograms;

import java.util.List;

import corpus.TextInstance;

public class Document {

	private TextInstance text;

	public Document(TextInstance text) {
		super();
		this.text = text;
	}

	private List<String> termsW;

	public List<String> getTerms() {
		return termsW;
	}

	public void setTerms(List<String> termsw) {
		this.termsW = termsw;

	}

	public TextInstance getTextInstance() {
		return text;
	}

}
