package jairescalante11.svm;

public class EnrichedSvmResult {

	private String author;
	private String textName;
	private SvmResult svmResult;

	public EnrichedSvmResult(String author, String textName, SvmResult svmResult) {
		super();
		this.author = author;
		this.textName = textName;
		this.svmResult = svmResult;
	}

	public String getCandidateAuthor() {
		return author;
	}

	public String getUnknownTextName() {
		return textName;
	}

	@Override
	public String toString() {
		return "EnrichedSvmResult [author=" + author + ", textName=" + textName
				+ ", svmResult=" + svmResult + "]";
	}

	public SvmResult getSvmResult() {
		return svmResult;
	}

}
