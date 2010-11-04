import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Reader extends Thread {
	Semaphore s = new Semaphore(1);
	String filesContents = "";
	String dir;
	String fileName = "";
	Integer count;

	public Reader(String pdir, String pFileName) {
		dir = pdir;
		fileName = pFileName;
	}

	private String getValidWord(String str) {
		if ((str.length() < 3) || (str.length() > 44))
			return "";
		StringBuilder sb = new StringBuilder();
		for (char c : str.toCharArray()) {
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
				sb.append(c);
			} else {
				return "";
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("deprecation")
	private String readAndCleanTextFile(String fileName) {
		File file = new File(fileName);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		StringBuffer r = new StringBuffer();
		try {
			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			// dis.available() returns 0 if the file does not have more lines.
			final String EmptyStr = "";
			while (dis.available() != 0) {
				String t = dis.readLine().trim();
				String[] vals = t.split(" ");
				String line = "";
				for (String string : vals) {
					t = getValidWord(string).toLowerCase().trim();
					int i = t.compareTo(EmptyStr);
					if (i == 0)
						continue;
					line += t + " ";
				}

				if (line.trim().compareTo("") != 0) {
					r.append(line.trim() + "\n");
					// System.out.println(line);
				}

			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();
			return r.toString();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private String readTextFile(String fileName) {
		File file = new File(fileName);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		StringBuffer r = new StringBuffer();
		try {
			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			while (dis.available() != 0) {
				r.append(dis.readLine() + "\n");
				count ++;
			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();
			return r.toString();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private String ReadFiles(String dir) {
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();

		count = listOfFiles.length;

		String temp = "";
		StringBuffer buffer = new StringBuffer();

		System.out.println("Bufferizing content from files from directory "
				+ dir);

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				temp = readAndCleanTextFile(listOfFiles[i].getAbsolutePath());
				buffer.append(temp + "\t");
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}
		return buffer.toString();
	}

	public String getFilesContent() {
		return filesContents;
	}

	public void run() {
		if (dir.compareTo("") == 0)
			filesContents = readTextFile(fileName);
		else
			filesContents = ReadFiles(dir);

	}
}
