package jairescalante11;

import jairescalante11.svm.EnrichedSvmResult;

import java.io.IOException;
import java.util.List;

public class Main {

	public static void main(String[] args) throws IOException {
		String inPath = "";
		if (args.length > 1 && args[0].equals("-i")) {
			inPath = args[1];
		} else {
			inPath = "../dataset/";
		}

		String outPath = "";
		if (args.length > 3 && args[2].equals("-o")) {
			outPath = args[3];
		} else {
			outPath = "./answers.json";
		}

		try {

			List<EnrichedSvmResult> results = new LocalHistogramAnalyzer(inPath).analyze();
			new ResultWriter(outPath).writeJsonFile(results);
		} catch (IOException up) {
			System.err.println("Fail. " + up.getMessage());
			throw up;
		}
	}
}
