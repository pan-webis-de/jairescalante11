package de.webis.nizza.localhistograms;

public class WeightedTermElement {

	private double localWeight;
	private long position;
	private String term;
	private int muPosition;

	public WeightedTermElement(double localWeight, long position, String term,
			int muPosition) {
		super();
		this.localWeight = localWeight;
		this.position = position;
		this.term = term;
		this.muPosition = muPosition;
	}

	public int getMuPosition() {
		return muPosition;
	}

	public void setMuPosition(int muPosition) {
		this.muPosition = muPosition;
	}

	public double getLocalWeight() {
		return localWeight;
	}

	public void setLocalWeight(double localWeight) {
		this.localWeight = localWeight;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	@Override
	public String toString() {
		return "WeightedTermElement [localWeight=" + localWeight
				+ ", position=" + position + ", term=" + term + ", muPosition="
				+ muPosition + "]";
	}

}
