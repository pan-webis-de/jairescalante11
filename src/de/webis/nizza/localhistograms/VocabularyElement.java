package de.webis.nizza.localhistograms;

public class VocabularyElement {

	private String term;
	private double weight;

	public VocabularyElement(String term) {
		super();
		this.term = term;
		this.weight = 0;
	}

	public String getTerm() {
		return term;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void addWeight(double toAddWeight) {
		this.weight = this.weight + toAddWeight;
	}

}
