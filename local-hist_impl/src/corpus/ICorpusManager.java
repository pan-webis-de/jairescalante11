package corpus;

import java.io.File;
import java.util.List;

public interface ICorpusManager {
	public TextInstance getNextText();
	public List<String> getAllAuthors();
	public int getTextCount();
	boolean validateUnknownAttribution(File text, String author);
	
}
