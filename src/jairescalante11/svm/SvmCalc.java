package jairescalante11.svm;

import jairescalante11.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class SvmCalc {

    Double[][] train; // train[i] -> one document
    private int dataCount;
    private List<Document> knownDocuments;

    public SvmCalc(List<Document> documents) {
        this.knownDocuments = documents
                .stream()
                .filter(e -> !e.getTextInstance().getTrueAuthor()
                        .contains("unknown")).collect(Collectors.toList());
        this.dataCount = knownDocuments.size();
        train = new Double[dataCount][];
        prepareSvm();
    }

    private void prepareSvm() {
        for (int i = 0; i < dataCount; i++) {
            double author = Double.parseDouble(knownDocuments.get(i)
                    .getTextInstance().getTrueAuthor().substring(9));
            List<Double> valueList = new ArrayList<>();
            valueList.add(author);
            valueList.addAll(knownDocuments.get(i).getLowbowHistogram());
            Double[] vals = valueList.toArray(new Double[valueList.size()]);
            train[i] = vals;
        }
    }

    public svm_model trainSvm() {
        svm_problem prob = new svm_problem();
        prob.y = new double[dataCount]; // authors
        prob.l = dataCount;
        prob.x = new svm_node[dataCount][]; // ngram data

        for (int i = 0; i < dataCount; i++) {
            Double[] features = train[i];
            prob.x[i] = new svm_node[features.length - 1];
            for (int j = 1; j < features.length; j++) {
                svm_node node = new svm_node();
                node.index = j;
                node.value = features[j];
                prob.x[i][j - 1] = node;
            }
            prob.y[i] = features[0]; // author
        }

        svm_parameter param = new svm_parameter();
        param.probability = 1;
        param.gamma = 0.5;
        param.nu = 0.5;
        param.C = 1;
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 20000;
        param.eps = 0.001;

        svm_model model = svm.svm_train(prob, param);

        return model;
    }

    public SvmResult evaluate(Double[] features, svm_model model, int classes) {
        svm_node[] nodes = new svm_node[features.length - 1];
        for (int i = 1; i < features.length; i++) {
            svm_node node = new svm_node();
            node.index = i;
            node.value = features[i];

            nodes[i - 1] = node;
        }

        int totalClasses = classes;
        int[] labels = new int[totalClasses];
        svm.svm_get_labels(model, labels);

        double[] prob_estimates = new double[totalClasses];
        double v = svm.svm_predict_probability(model, nodes, prob_estimates);

        System.out.println("(Actual:" + features[0] + " Prediction:" + v
                + " Confidence:" + prob_estimates[((int) v) - 1] + ")");
        return new SvmResult(features[0], v, prob_estimates[((int) v) - 1]);
    }

}
