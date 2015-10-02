package de.webis.nizza.localhistograms.svm;

public class SvmResult {

	private double actual;
	private double prediction;

	public SvmResult(Double actual, double prediction) {
		this.actual = actual;
		this.prediction = prediction;
	}

	public double getActual() {
		return actual;
	}

	public double getPrediction() {
		return prediction;
	}

	@Override
	public String toString() {
		return "SvmResult [actual=" + actual + ", prediction=" + prediction
				+ "]";
	}

}
