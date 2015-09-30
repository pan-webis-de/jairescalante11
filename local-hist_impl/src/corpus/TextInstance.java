package corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TextInstance {
	private String trueAuthor;
	private File textSource;
	
	public TextInstance(String author, File newTextSource)
	{
		trueAuthor = author;
		textSource = newTextSource;
	}
	
	/**
	 * Actually loads the text initially specified in the file passed via the constructor.
	 * The returned string may be very large, so rember to remove it after you're done
	 * using it. This object stores no text by itself.
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String getFullText() throws FileNotFoundException, IOException {
		List<String> fileContents = Files.readAllLines(textSource.toPath());
		StringBuilder fullFile = new StringBuilder();
		for (String line : fileContents)
		{
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
}
