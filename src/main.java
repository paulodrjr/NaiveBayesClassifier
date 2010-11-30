import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

public class main {

	static HashMap<String, Integer> vocabulary = new HashMap<String, Integer>();
	static Hashtable<String, String> config = new Hashtable<String, String>();
	static File localdir = new File(".");
	private static String VocabularyFileName = null;
	static double Pv[];
	static HashMap<String, Double>[] pWkV;
	static String internalClassNames[];
	static int nExamples;
	static int nExamplesCV;
	static int classCount = 0;
	static String[] classTexts;
	static int matchCount = 0;
	static int classificationSamples = 0;
	static MatchCounter[] matchCounters;

	private static boolean hasCharRepetitions(String word, int maxRepetitions) {
		int repetitions = 1;
		char buffer = '#';
		for (char c : word.toCharArray()) {
			if (c == buffer)
				repetitions++;
			else {
				buffer = c;
				repetitions = 1;
			}
			if (repetitions == maxRepetitions) {
				return true;
			}
		}
		return false;
	}

	private static String cleanPunctuation(String word) {
		StringBuilder sb = new StringBuilder();
		// discard invalid chars (return empty string if @ because it probably
		// represents an e-mail address, which itself is invalid
		for (char c : word.toCharArray()) {
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))
				sb.append(c);
			else if (c == '.' || c == ',' || c == ';')
				sb.append(' ');
			else if (c == '@')
				return "";
		}
		return sb.toString();
	}

	/**
	 * Get a word and returns a clean word, considered as "valid". Returns empty
	 * string otherwise.
	 * 
	 * @param word
	 * @return
	 */
	public static String getValidWord(String word) {

		// remove punctuation from words not classified as header words
		if (!isHeader(word))
			word = cleanPunctuation(word);

		// discard words with less than 3 or more then 44 chars
		if ((word.length() < 3) || (word.length() > 30))
			return "";

		// discard words with chars sequentially repeated 3 or more times
		if (hasCharRepetitions(word, 3))
			return "";

		StringBuilder sb = new StringBuilder();
		// after all the previous cleaning, discard words that still have
		// invalid chars
		for (char c : word.toCharArray()) {
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
				sb.append(c);
			} else {
				return "";
			}
		}
		// after all the previous cleaning, discard words that eventually has
		// been shrinked to less than 3 chars
		if ((sb.length() < 3) || (sb.length() > 30))
			return "";

		// after all the previous cleaning, discard words that eventually ended
		// with invalid chars repetitions
		if (hasCharRepetitions(sb.toString(), 3))
			return "";
		return sb.toString();
	}

	/**
	 * Create vocabulary (hashMap)
	 * 
	 * @param input
	 * @return
	 */
	public static HashMap<String, Integer> createVocabulary(String input) {

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		String word;
		final String EmptyStr = "";
		System.out.println("Creating vocabulary...");
		StringTokenizer st = new StringTokenizer(input);
		while (st.hasMoreElements()) {

			// get next token from text (assumes that the text is already clean)
			word = st.nextToken().toLowerCase().trim();

			int i = word.compareTo(EmptyStr);
			if (i == 0)
				continue;

			// update word count
			if (map.containsKey(word)) {
				int count = map.get(word);
				map.put(word, count + 1);
			} else
				map.put(word, 1);
		}
		System.out.println("Creating vocabulary done.");

		return cleanVocabulary(map);
		// return map;
	}

	/**
	 * Remove top 100 most frequent words, words that appear less than three
	 * times and empty string token
	 * 
	 * @param h
	 * @return
	 */
	private static HashMap<String, Integer> cleanVocabulary(
			HashMap<String, Integer> h) {
		HashMap<String, Integer> map = h;

		// remove words that appears less than three times in the vocabulary
		Iterator<String> it = map.keySet().iterator();
		ArrayList<String> list = new ArrayList<String>();
		while (it.hasNext()) {
			String key = it.next();
			if (map.get(key) < 3)
				list.add(key);
		}

		// remove empty token
		if (map.containsKey(""))
			list.add("");

		for (int i = 0; i < list.size(); i++) {
			map.remove(list.get(i));
		}

		Integer[] values = new Integer[100];
		String[] keys = new String[100];

		// init arrays
		for (int i = 0; i < values.length; i++) {
			values[i] = 0;
			keys[i] = "";
		}

		it = map.keySet().iterator();
		while (it.hasNext()) {
			String word = it.next();
			// get current word count
			int wCount = h.get(word);
			int i = 0;
			boolean done = false;
			while (i < values.length && !done) {
				/*
				 * if word count is bigger than the count stored in the current
				 * array position, move the existing values 1 position to the
				 * end and insert new value in the current position (do the same
				 * with the keywords array).
				 */
				if (wCount > values[i]) {
					for (int j = values.length - 1; j > i; j--) {
						values[j] = values[j - 1];
						keys[j] = keys[j - 1];
					}
					values[i] = wCount;
					keys[i] = word;
					done = true;
				}
				i++;
			}
		}

		// remove each word stored in the keywords array
		for (int i = 0; i < keys.length; i++)
			map.remove(keys[i]);

		return map;

	}

	public static boolean isHeader(String valor) {
		return (((valor.contains("subject:")))
				|| ((valor.contains("expires:")))
				|| ((valor.contains("distribution:")))
				|| ((valor.contains("lines:"))) || ((valor.contains("from:")))
				|| ((valor.contains("article-i.d.:")))
				|| ((valor.contains("in article")))
				|| ((valor.contains("reply-to:")))
				|| ((valor.contains("summary:")))
				|| ((valor.contains("supersedes:")))
				|| ((valor.contains("archive-name:")))
				|| ((valor.contains("keywords:")))
				|| ((valor.contains("last-modified:")))
				|| ((valor.contains("version:")))
				|| ((valor.contains("write to:")))
				|| ((valor.contains("telephone:")))
				|| ((valor.contains("fax:")))
				|| ((valor.contains("organization:")))
				|| ((valor.contains("nntp-posting-host:"))) || ((valor
				.contains("organization:")))

		);
	}

	/**
	 * Clean a text file, saving the resulting string in the folder "output"
	 * located in each class
	 * 
	 * @param fileName
	 * @return
	 */
	public static String cleanTextFile(String fileName) {

		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(fileName));
			try {
				String str;
				StringBuffer r = new StringBuffer();

				while ((str = in.readLine()) != null) {
					str = str.trim().toLowerCase();
					if (!isHeader(str)) {
						StringTokenizer st = new StringTokenizer(str);
						String cleanStr = "";
						while (st.hasMoreElements()) {
							String token = st.nextToken();
							cleanStr += getValidWord(token) + " ";
						}
						r.append(cleanStr);
					}
				}
				File file = new File(fileName);

				File dir = new File(file.getParent() + "/output/");
				if (!dir.exists())
					dir.mkdir();
				writeTextFile(r.toString(),
						dir.getAbsolutePath() + "/" + file.getName());
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

	/**
	 * Writes a text file
	 * 
	 * @param input
	 * @param fileName
	 */
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

	/**
	 * Cleans every text file located in the directory passed as parameter
	 * 
	 * @param dir
	 */
	private static void CleanFiles(String dir) {
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		System.out.println("Cleaning text files from directory " + dir);
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				cleanTextFile(listOfFiles[i].getAbsolutePath());
			}
		}

	}

	/**
	 * Reads an valid vocabulary text file representation to the internal class
	 * vocabulary variable
	 * 
	 * @return
	 */
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
			while (dis.available() != 0) {
				String s = dis.readLine();
				if (vocabulary.containsKey(s)) {
					vocabulary.put(s, vocabulary.get(s) + 1);
				} else {
					vocabulary.put(s, 1);
				}

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

	/**
	 * Calculates the probabilities for every class, token by token, storing
	 * each class set of probabilities in an internal HashMap and finally saving
	 * this HashMap to an text file.
	 * 
	 * @param classText
	 * @param className
	 * @return
	 */
	public static HashMap<String, Double> CalculateProbabilities(
			String classText, String className) {
		Set<String> e = vocabulary.keySet();

		WordCounter counter = new WordCounter(classText);

		int n = counter.getWordsCount();
		HashMap<String, Double> pWkV = new HashMap<String, Double>();

		Iterator<String> it = e.iterator();
		while (it.hasNext()) {
			String wk = it.next().toString();
			int nk = counter.getWordCountInText(wk);
			double d1 = (nk + 1);
			double d2 = (n + vocabulary.size());
			double p = d1 / d2;
			pWkV.put(wk, p);
		}
		// try {
		// savePercentages(pWkV, localdir.getCanonicalPath() + "/files/pWkV"
		// + className + ".probabilities");
		return pWkV;
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// return null;
		// }
	}

	@SuppressWarnings("unchecked")
	public static void train() {

		System.out
				.println("----------------------------------------------------------------------\nStarting training round...");

		/*
		 * HashMap array, with size equal to the class count, used to store the
		 * class tokens probabilities. Please refer to class notes and Naive
		 * Bayes base text for a better comprehension.
		 */
		pWkV = new HashMap[classCount];
		for (int i = 0; i < classCount; i++) {
			System.out.println("Training for class " + internalClassNames[i]);
			pWkV[i] = CalculateProbabilities(classTexts[i],
					internalClassNames[i]);
		}
		System.out
				.println("----------------------------------------------------------------------\nTraining done.");
	}

	private static void savePercentages(HashMap<String, Double> pWkV1,
			String filename) {
		writeTextFile(pWkV1.toString(), filename);
	}

	/**
	 * Prepares (clean) classes files, creates the vocabulary and initializes
	 * internal variables
	 * 
	 * @param trainDirs
	 * @param testDirs
	 * @param recreateVocabulary
	 * @return an String array, where every n-th position contains the
	 *         concatenated content text from each class
	 * 
	 */
	private static String[] prepare(String[] trainDirs,
			boolean recreateVocabulary) {

		String contents = "";
		String[] results = null;
		try {

			/*
			 * if (testDirs.length != trainDirs.length) throw new Exception(
			 * "O número de diretórios de teste e de treino tem que ser iguais!"
			 * );
			 */// maybe not

			// initializes class names array, based on folders within train
			// directory
			internalClassNames = getClassNames(trainDirs);

			classCount = trainDirs.length;
			File VocabularyFile = new File(VocabularyFileName);

			// clean files
			for (int i = 0; i < trainDirs.length; i++) {
				CleanFiles(trainDirs[i]);
			}

			// using Threads in order to improve reading performance
			Reader[] readers = new Reader[trainDirs.length];

			// initializing threads with reading path
			for (int i = 0; i < trainDirs.length; i++) {
				readers[i] = new Reader(trainDirs[i] + "/output/", "", true);
			}

			// start all threads for reading text files
			for (int i = 0; i < readers.length; i++) {
				readers[i].start();
			}

			// awaits for threads termination
			for (int i = 0; i < readers.length; i++)
				while (readers[i].isAlive()) {
				}

			results = new String[readers.length];

			// refer to class notes and Naive Bayes base text
			Pv = new double[readers.length];

			for (int i = 0; i < readers.length; i++)
				nExamples += readers[i].count;

			for (int i = 0; i < readers.length; i++) {
				results[i] = readers[i].getFilesContent() + "\n";
				writeTextFile(results[i], localdir.getCanonicalPath()
						+ "/files/output/" + internalClassNames[i] + ".text");
				contents += results[i];
				double v1 = readers[i].count;
				double v2 = nExamples;
				Pv[i] = v1 / v2;
			}

			if (recreateVocabulary) {
				// create vocabulary based on each clean class texts
				vocabulary = createVocabulary(contents);

				StringBuffer s = new StringBuffer();
				Iterator<String> it = vocabulary.keySet().iterator();
				while (it.hasNext()) {
					s.append(it.next() + "\n");
				}
				writeTextFile(s.toString(), localdir.getCanonicalPath()
						+ "/files/output/vocabulary.text");
				System.out.println("File " + localdir.getCanonicalPath()
						+ "/files/output/vocabulary.text"
						+ " successfully created.");
			} else if (VocabularyFile.exists())
				readVocabularyFile();
			else
				throw new IOException("File " + VocabularyFileName
						+ " not found!");
			return results;

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Returns an String array of classes names, based on an array of class
	 * folders
	 * 
	 * @param trainDirs
	 * @return
	 */
	private static String[] getClassNames(String[] trainDirs) {
		String[] r = new String[trainDirs.length];
		for (int i = 0; i < r.length; i++) {
			File f = new File(trainDirs[i]);
			r[i] = f.getName();
		}
		return r;
	}

	/**
	 * Lists files within a directory
	 * 
	 * @param dirPath
	 * @return
	 */
	private static String[] dirlist(String dirPath) {
		File dir = new File(dirPath);
		String[] chld = dir.list();
		if (chld == null) {
			System.out
					.println("Specified directory does not exist or is not a directory.");
			System.exit(0);
		} else {
			for (int i = 0; i < chld.length; i++) {
				chld[i] = dirPath + chld[i];
			}
			return chld;
		}
		return null;
	}

	/**
	 * Returns max class (position referent to max value) in an double array
	 * 
	 * @param t
	 *            Array de double
	 * @return int
	 */
	public static int max(double[] t) {
		double maximum = t[0]; // start with the first value
		int classMax = 0;
		for (int i = 1; i < t.length; i++) {
			if (t[i] > maximum) {
				maximum = t[i]; // new maximum
				classMax = i;
			}
		}
		return classMax;
	}

	/**
	 * reads a file using a Reader thread
	 */
	private static String readFile(String filename) {
		Reader reader;
		reader = new Reader("", filename, false);
		reader.start();
		while (reader.isAlive()) {
		}
		return reader.filesContents;
	}

	/**
	 * Classify text examples in a given array of test folders
	 * 
	 * @param input
	 */
	private static double classify(String[] input, String method, int step,
			String outputFileName) {
		StringBuffer stringBuffer = new StringBuffer();
		System.out
				.println("--------------------------------------------------\nStarting classification...");

		matchCounters = new MatchCounter[input.length];
		for (int i = 0; i < input.length; i++) {
			System.out.println("Classifying files from folder "
					+ internalClassNames[i]);
			// lists files in each folder
			String[] files = dirlist(input[i] + "/");
			matchCounters[i] = new MatchCounter(input.length);
			for (int j = 0; j < files.length; j++) {
				// classify each file content
				stringBuffer.append(classify(readFile(files[j]), files[j])
						+ "\n");
			}
		}

		double p1 = 0;
		double p2 = 0;

		StringBuffer m = new StringBuffer();
		for (int i = 0; i < classCount; i++) {
			m.append("Class: " + internalClassNames[i] + ". Hits: "
					+ matchCounters[i].getMatchCount() + "\n");
			for (int j = 0; j < classCount; j++) {
				if (internalClassNames[j].compareTo(internalClassNames[i]) == 0)
					continue;
				m.append(" ----> Classified as " + internalClassNames[j] + ": "
						+ matchCounters[i].getErrorCount(j) + "\n");
			}
			double r1 = (matchCounters[i].getMatchCount() + matchCounters[i]
					.getTotalErrorCount());
			p1 += r1;
			double r = 1 - (matchCounters[i].getTotalErrorCount() / r1);
			p2 += matchCounters[i].getTotalErrorCount();
			m.append(" ..... Class accuracy: "
					+ matchCounters[i].getTotalErrorCount() + "/("
					+ matchCounters[i].getMatchCount() + "+"
					+ matchCounters[i].getTotalErrorCount() + ")  =  " + r
					+ "\n");
		}

		// accuracy measured by confusion table
		m.append(" Total accuracy: " + (1 - (p2 / p1)) + "\n");

		// workaround: for some reason, division returned zero when was done
		// "in-line"
		p1 = (classificationSamples - matchCount);
		p2 = classificationSamples;
		double error = p1 / p2;

		// accuracy measured by counting matches
		m.append(" Global error: " + Double.toString(error));

		stringBuffer.append("Global error: " + Double.toString(error));

		try {
			File f = new File(localdir.getCanonicalPath() + "/files/" + method
					+ "/" + step);
			if (f.exists())
				deleteDirectory(f);

			f.mkdirs();

			if (method == "holdout")
				writeTextFile(m.toString(), outputFileName);
			else
				writeTextFile(m.toString(), localdir.getCanonicalPath()
						+ "/files/" + method + "/" + step + "/matriz.text");

			writeTextFile(stringBuffer.toString(), localdir.getCanonicalPath()
					+ "/files/" + method + "/" + step + "/classification.text");
			System.out
					.println("--------------------------------------------------\nClassification finalized with error "
							+ Double.toString(error)
							+ ". File "
							+ localdir.getCanonicalPath()
							+ "/files/classification.text sucessfully created.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return error;
	}

	private static int getClassIndexByName(String className) {
		for (int i = 0; i < internalClassNames.length; i++) {
			if (internalClassNames[i].compareTo(className) == 0)
				return i;
		}
		return -1;
	}

	/**
	 * Classifies an text input
	 * 
	 * @param input
	 * @param name
	 * @return
	 */
	private static String classify(String input, String name) {

		// classification samples count
		classificationSamples++;

		StringTokenizer tokenizer;

		tokenizer = new StringTokenizer(input);

		// double array used to store sum of each class classification
		double[] classification = new double[classCount];
		while (tokenizer.hasMoreElements()) {
			String token = getValidWord(tokenizer.nextToken()).trim()
					.toLowerCase();
			if (token.compareTo("") == 0)
				continue;
			for (int i = 0; i < classCount; i++) {
				if (pWkV[i].containsKey(token))
					classification[i] += pWkV[i].get(token);
			}
		}

		for (int i = 0; i < classCount; i++) {
			classification[i] += Pv[i] * classification[i];
		}

		int classMax = max(classification);

		boolean match;
		File f = new File(name);
		String className = "";
		if (f.isFile())
			className = f.getParentFile().getName();

		match = (className.compareTo(internalClassNames[classMax]) == 0);

		if (match) {
			matchCount++;
			matchCounters[getClassIndexByName(className)].incMatchCount();
		} else
			matchCounters[getClassIndexByName(className)]
					.incErrorCount(classMax);
		return (" {" + match + "}" + name + " = " + internalClassNames[classMax]);

	}

	private static void copyfile(String srFile, String dtFile) {
		try {
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);

			// For Append the file.
			// OutputStream out = new FileOutputStream(f2,true);

			// For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException ex) {
			System.out
					.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * used to put together files from test and train folders
	 * 
	 * @param testDir
	 * @param trainDir
	 */
	private static void copyFiles(String testDir, String trainDir) {
		String[] testFolders = dirlist(testDir);
		String[] trainFolders = dirlist(trainDir);

		for (int i = 0; i < testFolders.length; i++) {
			File folder = new File(testFolders[i]);
			File[] listOfTestFiles = folder.listFiles();

			for (File file : listOfTestFiles) {
				if (file.isDirectory())
					continue;
				copyfile(file.getAbsolutePath(),
						trainFolders[i] + "/" + file.getName());
			}
		}
	}

	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	/**
	 * Execute crossValidation over file set. First, the source set of files is
	 * randomized to an File Array. Next, files are copied from source folders
	 * to train and test folders, using the cross-validation method to select 9
	 * folds to train and 1 fold to test.
	 * 
	 * @param sourceDir
	 * @param testDir
	 * @param trainDir
	 */
	private static void crossValidateFiles(String sourceDir, String testDir,
			String trainDir, String outputFileName) {

		System.out
				.println("Executing Cross-Validation over file set. Please wait...");
		
		String[] sourceFolders = dirlist(sourceDir);// lists folders from source
		// folder

		File[][] randomizedFiles = new File[sourceFolders.length][];
		// randomize file array
		
		nExamplesCV = 0;
		for (int i = 0; i < sourceFolders.length; i++) {
			ArrayList<Integer> done = new ArrayList<Integer>();
			File folder = new File(sourceFolders[i]);
			File[] listOfFiles = folder.listFiles();
			randomizedFiles[i] = new File[listOfFiles.length];
			nExamplesCV += listOfFiles.length;
			for (int j = 0; j < listOfFiles.length; j++) {
				int n = -1;
				Random randomGenerator = new Random();
				while (done.contains(n) || n == -1) {
					n = randomGenerator.nextInt(listOfFiles.length);
				}
				done.add(n);
				randomizedFiles[i][j] = listOfFiles[n];
			}

		}
		
		double error[] = new double[10];
		double mean = 0;

		/*
		 * execute 10 times, each one over a different file set, formed by 9
		 * train folds and 1 test fold, where the test fold is k-fold and the
		 * train folds is the rest
		 */
		for (int k = 0; k < 10; k++) {
			System.out.println("\nStarting Cross-Validation (Step " + (k + 1)
					+ " of 10)");
			int d = 0;
			// delete train dir before creating train file set
			File dir = new File(testDir);
			deleteDirectory(dir);
			dir.mkdir();

			// delete test dir before creating test file set
			dir = new File(trainDir);
			deleteDirectory(dir);
			dir.mkdir();

			for (int i = 0; i < sourceFolders.length; i++) {
				System.out.println("\nCopying files...");
				// to get the rounded integer (e.g. 79.9 = 80)
				double tmp = (double) randomizedFiles[i].length / 10;
				int decimalPlace = 0;
				BigDecimal bd = new BigDecimal(tmp);
				bd = bd.setScale(decimalPlace, BigDecimal.ROUND_UP);
				tmp = bd.doubleValue();

				int foldSize = bd.intValue();
				d = k * foldSize;
				// copy train files, from 0 to start of test fold -1
				for (int j = 0; j < d; j++) {
					if (j >= randomizedFiles[i].length)
						continue;
					dir = new File(trainDir + "/"
							+ randomizedFiles[i][j].getParentFile().getName()
							+ "/");
					if (!dir.exists())
						dir.mkdirs();
					// copy file from source to dest folder
					copyfile(
							randomizedFiles[i][j].getAbsolutePath(),
							dir.getAbsolutePath() + "/"
									+ randomizedFiles[i][j].getName());
				}

				// copy test files, from start to the end of test fold
				for (int j = d; j < (k + 1) * foldSize; j++) {
					if (j >= randomizedFiles[i].length)
						continue;
					dir = new File(testDir + "/"
							+ randomizedFiles[i][j].getParentFile().getName()
							+ "/");
					if (!dir.exists())
						dir.mkdirs();
					copyfile(
							randomizedFiles[i][j].getAbsolutePath(),
							dir.getAbsolutePath() + "/"
									+ randomizedFiles[i][j].getName());

				}

				// copy train files, from the end of test fold +1 to the end of
				// the files array
				for (int j = (k + 1) * foldSize; j < randomizedFiles[i].length; j++) {

					dir = new File(trainDir + "/"
							+ randomizedFiles[i][j].getParentFile().getName()
							+ "/");
					if (!dir.exists())
						dir.mkdirs();
					copyfile(
							randomizedFiles[i][j].getAbsolutePath(),
							dir.getAbsolutePath() + "/"
									+ randomizedFiles[i][j].getName());

				}

			}
			// after create train and test files sets, execute train and test
			// tasks
			String[] testDirs;
			try {
				testDirs = dirlist(localdir.getCanonicalPath()
						+ "/files/20news-bydate-test/");

				String[] trainDirs = dirlist(localdir.getCanonicalPath()
						+ "/files/20news-bydate-train/");

				classTexts = prepare(trainDirs, true);

				train();

				error[k] = classify(testDirs, "crossvalidation", k + 1, "");
				mean += error[k];
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Done executing Cross-Validation over file set.");

		mean = mean / 10;
		double defaultErr = Math.sqrt((mean * (1 - mean)) / nExamplesCV);
		double ic1 = mean - 1.96 * defaultErr;
		double ic2 = mean + 1.96 * defaultErr;
		String confidenceInterval = ic1 + " < e < " + ic2;
		double err = 0;
		for (int i = 0; i < 10; i++) {
			err += Math.pow(error[i] - mean, 2);
		}

		err = Math.sqrt(err / 10);

		String output = "Cross-Validation Results\nError Mean: " + mean
				+ "\nStandard deviation: " + err + "\n" + confidenceInterval;
		try {
			// writeTextFile(output, localdir.getCanonicalPath()
			// + "/files/crossvalidation/error.text");
			writeTextFile(output, outputFileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Execute holdout over file set, taking 2/3 to train and 1/3 to test. Files
	 * are random taken.
	 * 
	 * @param sourceDir
	 * @param testDir
	 * @param trainDir
	 */
	private static void holdOutFiles(String sourceDir, String testDir,
			String trainDir) {

		System.out.println("Executing holdout over file set. Please wait...");

		String[] sourceFolders = dirlist(sourceDir);

		File dir = new File(testDir);
		deleteDirectory(dir);
		dir.mkdir();

		dir = new File(trainDir);
		deleteDirectory(dir);
		dir.mkdir();

		int lastPrint = -1;
		for (int i = 0; i < sourceFolders.length; i++) {
			ArrayList<Integer> done = new ArrayList<Integer>();
			File folder = new File(sourceFolders[i]);
			File[] listOfFiles = folder.listFiles();
			int fileCount = listOfFiles.length;

			int trainNum = (fileCount / 3) * 2;

			int percent = (i * 100) / sourceFolders.length;
			if ((percent % 10) == 0 && (percent != lastPrint)) {
				System.out.print(percent + "%...");
				lastPrint = percent;
			}

			// arquivos de treino
			for (int j = 0; j <= trainNum; j++) {
				if (listOfFiles[j].isDirectory())
					continue;

				int n = -1;
				Random randomGenerator = new Random();
				while (done.contains(n) || n == -1) {
					n = randomGenerator.nextInt(listOfFiles.length);
				}
				done.add(n);

				dir = new File(trainDir + "/"
						+ listOfFiles[n].getParentFile().getName());
				if (!dir.exists())
					dir.mkdir();
				copyfile(listOfFiles[n].getAbsolutePath(),
						dir.getAbsolutePath() + "/" + listOfFiles[n].getName());
			}

			// arquivos de teste
			for (int j = 0; j < fileCount; j++) {
				if (listOfFiles[j].isDirectory())
					continue;

				int n = j;

				if (done.contains(n))
					continue;
				done.add(n);

				dir = new File(testDir + "/"
						+ listOfFiles[n].getParentFile().getName());

				if (!dir.exists())
					dir.mkdir();
				copyfile(listOfFiles[n].getAbsolutePath(),
						dir.getAbsolutePath() + "/" + listOfFiles[n].getName());
			}
		}
		System.out.println("Done executing holdout over file set.");
	}

	private static void executeHoldout(String sourceDir, String outputFileName) {
		try {
			VocabularyFileName = localdir.getCanonicalPath()
					+ "/files/vocabulary.text";

			String testDir = localdir.getCanonicalPath()
					+ "/files/20news-bydate-test/";

			String trainDir = localdir.getCanonicalPath()
					+ "/files/20news-bydate-train/";

			// String sourceDir = localdir.getCanonicalPath()
			// + "/files/20news-bydate/";

			holdOutFiles(sourceDir, testDir, trainDir);

			String[] testDirs = dirlist(localdir.getCanonicalPath()
					+ "/files/20news-bydate-test/");
			String[] trainDirs = dirlist(localdir.getCanonicalPath()
					+ "/files/20news-bydate-train/");

			classTexts = prepare(trainDirs, true);

			train();

			classify(testDirs, "holdout", 1, outputFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void executeCrossValidation(String sourceDir,
			String outputFileName) {
		try {
			VocabularyFileName = localdir.getCanonicalPath()
					+ "/files/vocabulary.text";

			String testDir = localdir.getCanonicalPath()
					+ "/files/20news-bydate-test/";

			String trainDir = localdir.getCanonicalPath()
					+ "/files/20news-bydate-train/";

			crossValidateFiles(sourceDir, testDir, trainDir, outputFileName);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void init() {
		File temp;
		try {
			temp = new File(localdir.getCanonicalPath() + "/files/output/");
			if (temp.exists())
				deleteDirectory(temp);
			temp.mkdirs();
			
			temp = new File(localdir.getCanonicalPath() + "/files/20news-bydate-test/");
			if (temp.exists())
				deleteDirectory(temp);
			temp.mkdirs();
			
			temp = new File(localdir.getCanonicalPath() + "/files/20news-bydate-train/");
			if (temp.exists())
				deleteDirectory(temp);
			temp.mkdirs();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out
					.println(" =======================+++ Naive Bayes Text Classifier +++======================= ");
			System.out
					.println(" ======= Developed by\n  ++++ Celso Reis Alves\n  ++++ Leila Chan Choimei\n  ++++ Marcos S. Ferreira\n  ++++Paulo Degering R. Junior (paulo.usp.each@gmail.com)\n  ++++Roberta Lima");
			System.out
					.println(" ============== Artificial Intelligence / 2010 .:. EACH - USP ============= ");
			System.out
					.println(" ======= Copy, distribution and changes to this program is GRANTED =======");
			System.out
					.println(" =========================================================================");
			System.out
					.println("\n ======= To use this program, you need to have a folder with two or more classes from 20news dataset. All the train and test files MUST BE merged in this folder, in order to avoid learning deviation.");
			System.out
					.println(" ======= This program is meant to, in a single iteration, learn from training set and classify the test set.");
			System.out
					.println(" ======= Training set and data set are automatically chosen from data set source folder, depending on wich type of method is used to evaluate system performance (holdout or cross-validation).");
			System.out
					.println("\n ======= To start using the program, please supply: ");
			System.out
					.println("  +++ Source data set folder (e.g. C:\\20news) ");
			System.out.println("  +++ Output text file (e.g. C:\\output.txt) ");
			System.out
					.println("  +++ \"h\" to Holdout or \"c\" to Cross-Validation ");
			return;
		}
		init();
		args[0] = args[0] + "\\";
		File sourceDir = new File(args[0]);
		if (!sourceDir.exists()) {
			System.out.println("Source dir does not exists!");
			return;
		}

		if (args[2].compareTo("h") == 0)
			executeHoldout(args[0], args[1]);
		else if (args[2].compareTo("c") == 0)
			executeCrossValidation(args[0], args[1]);
		else
			System.out.println("Please, choose the validation method!");
	}
}