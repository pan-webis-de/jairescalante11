package corpus;

public class TextInstance {
	private String trueAuthor;
	private String fullText;

	public TextInstance(String author, String text) {
		trueAuthor = author;
		fullText = text;
	}

	public TextInstance(String text) {
		trueAuthor = "";
		fullText = text;
	}

	public String getFullText() {
		return fullText;
	}

	public String getTrueAuthor() {
		return trueAuthor;
	}
}
