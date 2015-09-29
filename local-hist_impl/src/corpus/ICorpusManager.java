package corpus;

import java.util.List;

public interface ICorpusManager {
	public TextInstance getNextText();
	public boolean validateAttribution(String textName, String author);
	public List<String> getAllAuthors();
	public int getTextCount();
	
}
