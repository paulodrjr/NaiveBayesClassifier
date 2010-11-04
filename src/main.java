import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class main {

	static Hashtable<String, String> vocabulary = new Hashtable<String, String>();
	static File localdir = new File(".");
	private static String VocabularyFileName = null;

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

			s = getValidWord(scanner.next()).toLowerCase();
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

	public static void CalculateProbabilities(String classText, String className) {
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
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void step1() {
		String contents;
		try {

			File VocabularyFile = new File(VocabularyFileName);

			Reader reader1 = null;
			Reader reader2 = null;
			Reader reader3 = null;

			String textClass1 = "";
			String textClass2 = "";
			String textClass3 = "";

			/*
			 * CleanFiles(localdir.getCanonicalPath() +
			 * "/files/20news-bydate-train/alt.atheism");
			 * 
			 * CleanFiles(localdir.getCanonicalPath() +
			 * "/files/20news-bydate-train/soc.religion.christian");
			 * 
			 * CleanFiles(localdir.getCanonicalPath() +
			 * "/files/20news-bydate-train/talk.religion.misc");
			 */

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

			double Pv1 = reader1.count / (nExamples);

			CalculateProbabilities(textClass1, "alt.atheism");
			CalculateProbabilities(textClass2, "soc.religion.christian");
			CalculateProbabilities(textClass3, "talk.religion.misc");

			/*
			 * Enumeration e = vocabulary.keys();
			 * 
			 * s = new StringBuffer(); while (e.hasMoreElements()) {
			 * s.append(e.nextElement().toString() + "\n"); }
			 * writeTextFile(s.toString(), localdir.getCanonicalPath() +
			 * "/files/vocabulary.text"); System.out.println("File " +
			 * localdir.getCanonicalPath() + "/files/vocabulary.text" +
			 * " successfully created.");
			 */
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void savePercentages(HashMap<String, Double> pWkV1,
			String filename) {
		writeTextFile(pWkV1.toString(), filename);
	}

	public static void main(String[] args) {
		// WordCounter counter = new WordCounter();
		// int n = counter
		// .getDistinctWordsCount("As informações que os escritores antigos deixaram sobre sua vida são extremamente pobres, mas sua influência atravessou os séculos e é percebida até nos dias de hoje. Na Antiguidade ficou conhecido graças às suas esculturas de atletas e a uma colossal estátua de Hera, esculpida em marfim e ouro e instalada no templo da deusa em Argos. Mas a enorme fama de Policleto derivou principalmente de seu tratado teórico");
		// System.out.println(n);
		// System.out.println(counter.getWordCountInText("os"));
		try {
			VocabularyFileName = localdir.getCanonicalPath()
					+ "/files/vocabulary.text";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		step1();
	}
}
