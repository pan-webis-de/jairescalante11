package corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TextInstance {
	private String trueAuthor;
	private File textSource;
	private boolean hasText;

	public TextInstance(String author, File newTextSource) {
		trueAuthor = author;
		textSource = newTextSource;
		hasText = false;
	}

	public String getFullText() throws FileNotFoundException, IOException {
		List<String> fileContents = Files.readAllLines(textSource.toPath());
		StringBuilder fullFile = new StringBuilder();
		for (String line : fileContents) {
			fullFile.append(line);
			fullFile.append("\n");
		}
		return fullFile.toString();
	}

	public File getTextSource() {
		return textSource;
	}

	public String getTrueAuthor() {
		return trueAuthor;
	}

	public boolean hasText() {
		return hasText;
	}
}
