import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class main {

	static Hashtable<String, String> vocabulary = new Hashtable<String, String>();
	static Hashtable<String, String> config = new Hashtable<String, String>();
	static File localdir = new File(".");
	private static String VocabularyFileName = null;
	static double Pv1 = 0;
	static double Pv2 = 0;
	static double Pv3 = 0;
	static HashMap<String, Double> pWkV1;
	static HashMap<String, Double> pWkV2;
	static HashMap<String, Double> pWkV3;

	public static String getValidWord(String str) {
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

	public static Hashtable<String, String> createVocabulary(String input) {
		Scanner scanner = new Scanner(input);
		scanner.useDelimiter(" ");
		Hashtable<String, String> h = new Hashtable<String, String>();
		String s;
		final String EmptyStr = "";
		System.out.println("Creating vocabulary...");
		while (scanner.hasNext()) {
			s = getValidWord(scanner.next()).toLowerCase().trim();
			// s = scanner.next().trim();
			int i = s.compareTo(EmptyStr);
			if ((h.contains(s)) || (i == 0))
				continue;
			h.put(s, s);
		}
		System.out.println("Creating vocabulary done.");
		return h;
	}

	public static String cleanTextFile(String fileName) {
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
			boolean startCommentsRead = false;
			boolean footerFound = false;
			// dis.available() returns 0 if the file does not have more lines.

			// skip first text block, which always contains the header
			while ((dis.available() != 0) && !startCommentsRead) {
				startCommentsRead = (dis.readLine().trim().compareTo("") == 0);
			}

			while ((dis.available() != 0) && !footerFound) {
				String s = dis.readLine();
				Pattern p = Pattern.compile(".*@.*");
				Matcher m = p.matcher(s);
				if (!m.matches())
					r.append(s + "\n");
			}

			// dispose all the resources after using them.
			File dir = new File(file.getParent() + "/output/");
			if (!file.exists())
				dir.mkdir();
			writeTextFile(r.toString(), dir.getAbsolutePath() + "/"
					+ file.getName());
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

	private static void writeTextFile(String input, String fileName) {
		try {
			FileWriter outFile = new FileWriter(fileName);
			PrintWriter out = new PrintWriter(outFile);
			out.println(input);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void CleanFiles(String dir) {
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		System.out.println("Cleaning text files from directory " + dir);
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				cleanTextFile(listOfFiles[i].getAbsolutePath());
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}

	}

	private static String readVocabularyFile() {
		File file = new File(VocabularyFileName);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		StringBuffer r = new StringBuffer();
		System.out
				.println("Reading vocabulary from file " + VocabularyFileName);
		try {
			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			// dis.available() returns 0 if the file does not have more lines.
			final String EmptyStr = "";
			while (dis.available() != 0) {
				String s = dis.readLine();
				if (vocabulary.contains(s))
					continue;
				vocabulary.put(s, s);
			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();
			System.out.println("Reading vocabulary done.");
			return r.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static HashMap<String, Double> CalculateProbabilities(
			String classText, String className) {
		Enumeration<String> e = vocabulary.keys();

		WordCounter counter = new WordCounter(classText);
		int n = counter.getWordsCount();
		HashMap<String, Double> pWkV = new HashMap<String, Double>();

		while (e.hasMoreElements()) {
			String wk = e.nextElement().toString();
			int nk = counter.getWordCountInText(wk);
			double d1 = (nk + 1);
			double d2 = (n + vocabulary.size());
			double p = d1 / d2;
			pWkV.put(wk, p);
		}
		try {
			savePercentages(pWkV, localdir.getCanonicalPath() + "/files/pWkV"
					+ className + ".text");
			return pWkV;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}

	public static void train() {
		String contents;
		try {

			File VocabularyFile = new File(VocabularyFileName);

			Reader reader1 = null;
			Reader reader2 = null;
			Reader reader3 = null;

			String textClass1 = "";
			String textClass2 = "";
			String textClass3 = "";

			CleanFiles(localdir.getCanonicalPath()
					+ "/files/20news-bydate-test/alt.atheism");

			CleanFiles(localdir.getCanonicalPath()
					+ "/files/20news-bydate-test/soc.religion.christian");

			CleanFiles(localdir.getCanonicalPath()
					+ "/files/20news-bydate-test/talk.religion.misc");
			
			CleanFiles(localdir.getCanonicalPath()
					+ "/files/20news-bydate-train/alt.atheism");

			CleanFiles(localdir.getCanonicalPath()
					+ "/files/20news-bydate-train/soc.religion.christian");

			CleanFiles(localdir.getCanonicalPath()
					+ "/files/20news-bydate-train/talk.religion.misc");

			// using Threads to in order to improve reading performance

			reader1 = new Reader(localdir.getCanonicalPath()
					+ "/files/20news-bydate-train/alt.atheism/output", "");
			reader2 = new Reader(
					localdir.getCanonicalPath()
							+ "/files/20news-bydate-train/soc.religion.christian/output",
					"");
			reader3 = new Reader(localdir.getCanonicalPath()
					+ "/files/20news-bydate-train/talk.religion.misc/output",
					"");

			reader1.start();
			reader2.start();
			reader3.start();

			while (reader1.isAlive() || reader2.isAlive() || reader3.isAlive()) {
			}

			textClass1 = (reader1.getFilesContent() + "\n");
			writeTextFile(textClass1, localdir.getCanonicalPath()
					+ "/files/text1.text");
			textClass2 = (reader2.getFilesContent() + "\n");
			writeTextFile(textClass2, localdir.getCanonicalPath()
					+ "/files/text2.text");
			textClass3 = (reader3.getFilesContent());
			writeTextFile(textClass3, localdir.getCanonicalPath()
					+ "/files/text3.text");

			contents = textClass1 + textClass2 + textClass3;
			if (VocabularyFile.exists())
				readVocabularyFile();
			else
				vocabulary = createVocabulary(contents);

			Integer nExamples = reader1.count + reader2.count + reader3.count;
			double v1 = reader1.count;
			double v2 = nExamples;
			Pv1 = v1 / v2;
			v1 = reader2.count;			
			Pv2 = v1 / v2;
			v1 = reader3.count;			
			Pv3 = v1 / v2;
			config.put("Pv1", Double.toString(Pv1));
			config.put("Pv2", Double.toString(Pv2));
			config.put("Pv3", Double.toString(Pv3));

			saveConfig(localdir.getCanonicalPath() + "/files/config.text");

			pWkV1 = CalculateProbabilities(textClass1, "alt.atheism");
			pWkV2 = CalculateProbabilities(textClass2, "soc.religion.christian");
			pWkV3 = CalculateProbabilities(textClass3, "talk.religion.misc");

			Enumeration e = vocabulary.keys();

			StringBuffer s = new StringBuffer();
			while (e.hasMoreElements()) {
				s.append(e.nextElement().toString() + "\n");
			}
			writeTextFile(s.toString(), localdir.getCanonicalPath()
					+ "/files/vocabulary.text");
			System.out.println("File " + localdir.getCanonicalPath()
					+ "/files/vocabulary.text" + " successfully created.");

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void saveConfig(String filename) {
		writeTextFile(config.toString(), filename);
	}

	private static void savePercentages(HashMap<String, Double> pWkV1,
			String filename) {
		writeTextFile(pWkV1.toString(), filename);
	}

	public static void main(String[] args) {
		try {
			VocabularyFileName = localdir.getCanonicalPath()
					+ "/files/vocabulary.text";
			train();			
			
			classify(localdir.getCanonicalPath()
					+ "/files/20news-bydate-test/alt.atheism/output/");
			classify(localdir.getCanonicalPath()
					+ "/files/20news-bydate-test/talk.religion.misc/output/");
			classify(localdir.getCanonicalPath()
					+ "/files/20news-bydate-test/soc.religion.christian/output/");	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	private static void classify(String fileName) {		
		// HashMap<String, Double> PwKv1 = getProbabilities("alt.atheism");
		Reader reader;
		
		reader = new Reader(fileName, "");
		reader.start();
		while (reader.isAlive()) {
		}
		StringTokenizer tokenizer = new StringTokenizer(reader.getFilesContent());
		
		double c1 = 0;
		double c2 = 0;
		double c3 = 0;
		while (tokenizer.hasMoreElements()) {
			String token = getValidWord(tokenizer.nextToken()).trim().toLowerCase();
			if (token.compareTo("") == 0)
				continue;			
			if (pWkV1.containsKey(token)) {
				c1 += pWkV1.get(token);
			}
			if (pWkV2.containsKey(token)) {
				c2 += pWkV2.get(token);
			}
			if (pWkV3.containsKey(token)) {
				c3 += pWkV3.get(token);
			}	

		}
		c1 = Pv1 * c1;
		c2 = Pv2 * c2;
		c3 = Pv3 * c3;
		if ((c1 > c2) && (c1 > c3))
			System.out.println("Classe 1!");
		else if ((c2 > c1) && (c2 > c3))
			System.out.println("Classe 2!");
		else if ((c3 > c1) && (c3 > c3))
			System.out.println("Classe 3!");
		else
			System.out.println("Whatever: " + c1 + "; " + c2 + ";" + c3);
	}

	private static HashMap<String, Double> getProbabilities(String className) {
		try {
			String filename = localdir.getCanonicalPath() + "/files/pWkV"
					+ className + ".text";

			System.out.println("Reading probabilities from file " + filename);

			File file = new File(filename);
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			DataInputStream dis = null;
			StringBuffer r = new StringBuffer();

			HashMap<String, Double> res = new HashMap<String, Double>();

			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			// dis.available() returns 0 if the file does not have more lines.
			final String EmptyStr = "";
			while (dis.available() != 0) {
				String s = dis.readLine();
				// String[] values = s.split(=)
			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();
			System.out.println("Reading vocabulary done.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
}
