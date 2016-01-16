package jairescalante11.svm;

public class SvmResult {

	private double actual;
	private double prediction;
	private double confidence;

	public SvmResult(Double actual, double prediction, double confidence) {
		this.actual = actual;
		this.prediction = prediction;
		this.confidence = confidence;
	}

	public double getConfidence() {
		return confidence;
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
				+ ", confidence=" + confidence + "]";
	}

}
