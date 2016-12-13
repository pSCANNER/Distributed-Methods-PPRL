package rosita.linkage.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileIO 
{
	/**
	 * Given some input string, append it to a file located at 'filename'
	 * @param input - The data to append to the file
	 * @param filename - The path to the file to be written to
	 */
	public static void AppendToFile(String input, String filename)
	{

		BufferedWriter b = null;
		try {

			b = new BufferedWriter(new FileWriter(filename, true));
			b.write(input);
			b.close();

		} catch (IOException e1) {
			System.out.println("Failed to buffered writer. Check the filename?");
			e1.printStackTrace();
		}

	}
}
