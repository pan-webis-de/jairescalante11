package libsvm.kernel;

public interface Kernel {

	public double calculateDistance(double[] bolhAuthorP, double[] bolhAuthorQ);

	public default double calculateKernel(double[] bolhAuthorP,
			double[] bolhAuthorQ) {
		double scale = 1; // TODO find scale...
		double distance = calculateDistance(bolhAuthorP, bolhAuthorQ);
		return Math.exp(-((distance * distance) / scale));

	}
}
