package corpus;

import java.io.File;
import java.util.List;

public interface ICorpusManager {
	public TextInstance getNextText();
	public List<String> getAllAuthors();
	public int getTextCount();
	public int getUnknownTextCount();
	boolean validateUnknownAttribution(File text, String author);
	public TextInstance getUnknownText();
}
