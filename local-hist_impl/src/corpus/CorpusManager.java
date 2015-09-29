package corpus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import util.Utilities;

public class CorpusManager implements ICorpusManager {
	private Path corpusLocation;
	private String metaFileName = "meta-file.json";
	private String groundTruthFileName = "ground-truth.json";
	private String encoding;
	private String unknownFolder;
	private String language;
	private List<String> authors;
	private List<TextInstance> knownTexts;
	private List<File> unknownTexts;
	
	private Iterator<TextInstance> knownTextIterator;

	public CorpusManager(String location) throws IOException {
		corpusLocation = new File(location).toPath();
		File metaFile = new File(corpusLocation.toFile(), metaFileName);
		File groundTruthFile = new File(corpusLocation.toFile(), groundTruthFileName);

		if (!metaFile.exists()) {
			throw new IOException("Could not find meta file '" + metaFile.toString() + "'");
		}

		if (!groundTruthFile.exists()) {
			throw new IOException("Could not find groundTruth file '" + groundTruthFile.toString() + "'");
		}

		JsonObject metadata = null;
		JsonObject groundData = null;
		
		try (InputStream metaInputStream = new FileInputStream(metaFile);
				JsonReader metaReader = Json.createReader(metaInputStream);
				InputStream groundInputStream = new FileInputStream(groundTruthFile);
				JsonReader groundReader = Json.createReader(groundInputStream);) {

			metadata = metaReader.readObject();
			groundData = groundReader.readObject();
		}
		catch (Exception e) {
			throw new IOException("Failed to read JSON: " + e.getMessage());
		}
		
		encoding = metadata.getString("encoding");
		unknownFolder = metadata.getString("folder");
		language = metadata.getString("language");
		
		authors = new ArrayList<String>();
		for(JsonObject author : metadata.getJsonArray("candidate-authors").getValuesAs(JsonObject.class))
		{
			authors.add(author.getString("author-name"));
		}
		
		unknownTexts = new ArrayList<File>();
		for(JsonObject text : metadata.getJsonArray("unknown-texts").getValuesAs(JsonObject.class))
		{
			unknownTexts.add(new File(unknownFolder, text.getString("unknown-text")));
		}
		
		discoverKnownTexts();
		
		knownTextIterator = knownTexts.iterator();
	}

	private void discoverKnownTexts() {
		assert(authors.size() > 0);
		
		for (String author : authors)
		{
			File authorFolder = new File(corpusLocation.toFile(), author);
			if ((!authorFolder.exists()) || (!authorFolder.isDirectory()))
			{
				System.err.println("Could not open folder " + authorFolder + ", skipping.");
				continue;
			}
			
			List<Path> texts = Utilities.getDirectoryContents(authorFolder);
			knownTexts = new ArrayList<TextInstance>();
			for (Path text : texts)
			{
				if (text.toFile().exists())
				{
					TextInstance instance = new TextInstance(author, text.toFile());
					knownTexts.add(instance);
				}
				else
				{
					System.err.println("Could not locate file " + text.toString() + ", skipping.");
					continue;
				}
			}
		}
	}
	
	@Override
	public TextInstance getNextText() {
		if (knownTextIterator.hasNext())
		{
			return knownTextIterator.next();
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean validateAttribution(String textName, String author) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getAllAuthors() {
		return authors;
	}

	@Override
	public int getTextCount() {
		return knownTexts.size();
	}

	public static void main(String[] args) {
		try {
			CorpusManager c = new CorpusManager("Corpus/NEW CORPORA/C10/");
			TextInstance i = c.getNextText();
			System.out.println(i.getTrueAuthor());
			System.out.println(i.getTextSource());
			System.out.println(i.getFullText());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
