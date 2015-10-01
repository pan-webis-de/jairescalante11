package de.webis.nizza.localhistograms;

public class WeightedTerm {

	private String term;
	private double weight;

	public String getTerm() {
		return term;
	}

	public double getWeight() {
		return weight;
	}

	public WeightedTerm(String term, double weight) {
		super();
		this.term = term;
		this.weight = weight;
	}

}
