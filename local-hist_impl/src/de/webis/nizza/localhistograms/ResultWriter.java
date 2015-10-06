package de.webis.nizza.localhistograms;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import de.webis.nizza.localhistograms.svm.EnrichedSvmResult;

public class ResultWriter {

	private String path;

	public ResultWriter(String path) {
		super();
		this.path = path;
	}

	public void writeJsonFile(List<EnrichedSvmResult> results)
			throws FileNotFoundException {

		JsonArrayBuilder answerJsonArray = Json.createArrayBuilder();

		for (EnrichedSvmResult result : results) {

			JsonObject answerJson = Json.createObjectBuilder()
					.add("unknown-text", result.getUnknownTextName())
					.add("author", result.getCandidateAuthor())
					.add("score", 1.0).build();

			answerJsonArray.add(answerJson);

		}

		JsonObject resultJson = Json.createObjectBuilder()
				.add("answers", answerJsonArray.build()).build();

		Map<String, Object> properties = new HashMap<>(1);
		properties.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonWriterFactory writerFactory = Json.createWriterFactory(properties);

		JsonWriter resultWriter = writerFactory
				.createWriter(new FileOutputStream(path));
		resultWriter.writeObject(resultJson);
		resultWriter.close();
	}

}
