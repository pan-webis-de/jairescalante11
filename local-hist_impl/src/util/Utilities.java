package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

public class Utilities {
	public static ArrayList<Path> getDirectoryContents(File folder) 
	{
		ArrayList<Path> files = new ArrayList<Path>();
		try {
			Iterator<Path> fileIter = Files.list(folder.toPath()).iterator();
			while (fileIter.hasNext())
			{
				files.add(fileIter.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return files;
	}
}
