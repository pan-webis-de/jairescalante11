package libsvm.kernel;

public class Chi2Kernel {

	public double calculateKernel(double[] bolhAuthorP, double[] bolhAuthorQ
	// , int singleBolhSize
	) {
		if (bolhAuthorP.length != bolhAuthorQ.length) {
			throw new IllegalArgumentException();
		}

		double sum = 0;
		// for (int i = 0; i < bolhAuthorP.length; i += singleBolhSize) {
		// // iterate over single BOLHs
		//
		// }

		for (int i = 0; i < bolhAuthorP.length; i++) {
			double singleSum = (Math.pow(bolhAuthorP[i] - bolhAuthorQ[i], 2))
					/ (bolhAuthorP[i] + bolhAuthorQ[i]);
			sum += singleSum;
		}

		double distance = Math.sqrt(sum);
		return distance;
	}
}
