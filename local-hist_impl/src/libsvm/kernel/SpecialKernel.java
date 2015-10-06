package libsvm.kernel;

import libsvm.svm_node;

public interface SpecialKernel {

	public double calculateKernel(svm_node[] svm_nodes, svm_node[] svm_nodes2);

}
