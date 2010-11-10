import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Reader extends Thread {
	String filesContents = "";
	String dir;
	String fileName = "";
	int count = 0;
	boolean printMessages;

	public Reader(String pdir, String pFileName, boolean printOut) {
		dir = pdir;
		fileName = pFileName;
		this.printMessages = printOut;
	}

	@SuppressWarnings("deprecation")
	private String readAndCleanTextFile(String fileName) {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(fileName));
			try {
				String str;
				StringBuffer r = new StringBuffer();

				while ((str = in.readLine()) != null) {

					StringTokenizer st = new StringTokenizer(str);
					String cleanStr = "";
					while (st.hasMoreElements()) {
						String token = st.nextToken();
						cleanStr += main.getValidWord(token).trim().toLowerCase()
								+ " ";
//						cleanStr += token.trim().toLowerCase()	+ " ";
					}
					r.append(cleanStr);
				}
				return r.toString();
			} finally {
				in.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
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
				count++;
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

		if (printMessages)
			System.out.println("Buffering content from files from directory "
					+ dir);

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				temp = readAndCleanTextFile(listOfFiles[i].getAbsolutePath())
						.trim();
				buffer.append(temp + "\n");
			}
		}
//		System.out.println(buffer);
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
