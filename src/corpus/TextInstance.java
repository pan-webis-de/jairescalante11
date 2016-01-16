package corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
	 * using it. This object stores no text by itself. Bad characters in the text are 
	 * ignored.
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String getFullText() throws FileNotFoundException, IOException {
		
		InputStream in = new FileInputStream(textSource);
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.IGNORE);
		
		final char[] buffer = new char[1024];
	    final StringBuilder out = new StringBuilder();
	    
	    try (Reader reader = new InputStreamReader(in, decoder)) {
	        for (;;) {
	            int rsz = reader.read(buffer, 0, buffer.length);
	            if (rsz < 0)
	                break;
	            out.append(buffer, 0, rsz);
	        }
	    }
	    catch (UnsupportedEncodingException ex) {
	        /* ... */
	    }
	    catch (IOException ex) {
	        /* ... */
	    }
	    
	    return out.toString();
	}
	
	public File getTextSource() {
		return textSource;
	}
	
	public String getTrueAuthor() {
		return trueAuthor;
	}
}
