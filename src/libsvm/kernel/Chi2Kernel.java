package libsvm.kernel;

import libsvm.svm_node;

public class Chi2Kernel implements SpecialKernel {

	private static int histogramNumber = 0;
	private static int histogramLength = 2500;

	@Override
	public double calculateKernel(svm_node[] bolhAuthorP, svm_node[] bolhAuthorQ) {

		if (bolhAuthorP.length != bolhAuthorQ.length) {
			throw new IllegalArgumentException();
		}

		double sum = 0;

		for (int i = 0 * histogramLength; i < (histogramNumber + 1)
				* histogramLength; i++) {
			double toPow = bolhAuthorP[i].value - bolhAuthorQ[i].value;
			double up = Math.pow(toPow, 2);

			double down = bolhAuthorP[i].value + bolhAuthorQ[i].value;

			double singleSum = up / down;
			sum += singleSum;
		}

		double distance = Math.sqrt(sum);
		double scale = 1; // TODO find scale...
		histogramNumber++;
		return Math.exp(-((distance * distance) / scale));
	}
}
