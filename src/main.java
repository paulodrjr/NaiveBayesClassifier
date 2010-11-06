import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class main {

	static HashMap<String, Integer> vocabulary = new HashMap<String, Integer>();
	static Hashtable<String, String> config = new Hashtable<String, String>();
	static File localdir = new File(".");
	private static String VocabularyFileName = null;
	static double Pv[];
	static HashMap<String, Double>[] pWkV;
	static String internalClassNames[];
	static int nExamples;
	static int classCount = 0;
	static String[] classTexts;
	static int matchCount = 0;
	static int classificationSamples = 0;

	/**
	 * Retorna palavras "válidas", ou string vazio caso contrário.
	 * @param str
	 * @return
	 */
	public static String getValidWord(String str) {
		//deixar de fora palavras com tamanho menor que 3 ou maior que 44
		if ((str.length() <= 3) || (str.length() > 44))
			return "";
		StringBuilder sb = new StringBuilder();
		//deixar de fora palavras com caracteres inválidos
		for (char c : str.toCharArray()) {
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
				sb.append(c);
			} else {
				return "";
			}
		}
		return sb.toString();
	}

	/**
	 * Cria o vocabulário (hashMap)
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
			
			//tenta eliminar tokens indesejáveis
			word = getValidWord(st.nextToken()).toLowerCase().trim();

			int i = word.compareTo(EmptyStr);
			if (i == 0)
				continue;
			
			// atualiza contagem da palavra
			if (map.containsKey(word)) {
				int count = map.get(word);
				map.put(word, count + 1);
			} else
				map.put(word, 1);
		}
		System.out.println("Creating vocabulary done.");
		// HashMap<String, Integer> teste = cleanVocabulary(map);
		// System.out.println(teste);
		return cleanVocabulary(map);
	}
	
	/**
	 * Tira do vocabulário as 100 palavras mais frequentes
	 * @param h
	 * @return
	 */
	private static HashMap<String, Integer> cleanVocabulary(
			HashMap<String, Integer> h) {
		HashMap<String, Integer> map = h;

		Integer[] values = new Integer[100];
		String[] keys = new String[100];

		// inicializar arrays
		for (int i = 0; i < values.length; i++) {
			values[i] = 0;
			keys[i] = "";
		}

		Iterator<String> it = h.keySet().iterator();
		while (it.hasNext()) {
			String word = it.next();
			//pega a contagem de cada word
			int v = h.get(word);
			int i = 0;
			boolean done = false;			
			while (i < values.length && !done) {
				//se a contagem de word for maior que a posição corrente, mover contadores anteriores e inserir nova palavra
				if (v > values[i]) {
					for (int j = values.length - 1; j > i; j--) {
						values[j] = values[j - 1];
						keys[j] = keys[j - 1];
					}
					values[i] = v;
					keys[i] = word;
					done = true;
				}
				i++;
			}
		}

		//remover do hashmap as 100 palavras encontradas
		for (int i = 0; i < keys.length; i++)
			map.remove(keys[i]);

		return map;

	}
	
	/**
	 * Limpa um arquivo de texto, salvando a saída na pasta "output" da classe
	 * @param fileName
	 * @return
	 */
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

			// tenta deixar de fora o cabeçalho do e-mail, pulando todas as linhas até encontrar uma linha em branco.
			while ((dis.available() != 0) && !startCommentsRead) {
				startCommentsRead = (dis.readLine().trim().compareTo("") == 0);
			}

			while ((dis.available() != 0) && !footerFound) {
				String s = dis.readLine();
				//padrão regex para ignorar palavras com @. Pode ser extendido para outros tipos de termos
				Pattern p = Pattern.compile(".*@.*");
				Matcher m = p.matcher(s);
				if (!m.matches())
					r.append(s + "\n");
			}

			File dir = new File(file.getParent() + "/output/");
			if (!dir.exists())
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

	/**
	 * Escreve um arquivo de texto
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
	 * limpa os arquivos de um diretório, segundo escolhas flexíveis
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
	 * lê o arquivo de vocabulário para o vocabulário em memória
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
			final String EmptyStr = "";
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
	 * Calcula as probabilidades para cada classe, termo a termo
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
		try {
			savePercentages(pWkV, localdir.getCanonicalPath() + "/files/pWkV"
					+ className + ".text");
			return pWkV;
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	public static void train() {
		
		System.out.println("----------------------------------------------------------------------\nStarting training round...");
		//array de hashmap String-Double (um para cada classe), onde armazenar as probabilidade de cada classe.
		//olhar algoritmo do texto-base para melhor entendimento		
		pWkV = new HashMap[classCount];
		for (int i = 0; i < classCount; i++) {
			System.out.println("Training for class " + internalClassNames[i]);
			pWkV[i] = CalculateProbabilities(classTexts[i],
					internalClassNames[i]);
		}
		System.out.println("----------------------------------------------------------------------\nTraining done.");
	}

	private static void savePercentages(HashMap<String, Double> pWkV1,
			String filename) {
		writeTextFile(pWkV1.toString(), filename);
	}
    /**
     * prepara os arquivos, cria o vocabulário e inicializa os valores da instância
     * @param trainDirs
     * @param testDirs
     * @param recreateVocabulary
     * @return um array de string, onde cada i-ésima posição contém o conteúdo concatenado dos textos da classe
     */
	private static String[] prepare(String[] trainDirs, String[] testDirs,
			boolean recreateVocabulary) {
		
		String contents = "";
		String[] results = null;
		try {

			if (testDirs.length != trainDirs.length)
				throw new Exception(
						"O número de diretórios de teste e de treino tem que ser iguais!");
			
			//inicializa o vetor de nomes de classes
			internalClassNames = getClassNames(trainDirs);

			classCount = trainDirs.length;
			File VocabularyFile = new File(VocabularyFileName);			
			
			//efetua uma limpeza nos arquivos, direcionando a saída para o subdiretório "output" de cada classe
			for (int i = 0; i < trainDirs.length; i++) {
				CleanFiles(trainDirs[i]);
				CleanFiles(testDirs[i]);
			}

			// using Threads in order to improve reading performance
			Reader[] readers = new Reader[trainDirs.length];
			
			// inicializando threads com caminho de leitura
			for (int i = 0; i < trainDirs.length; i++) {
				readers[i] = new Reader(trainDirs[i] + "/output/", "", true);
			}
			
			//inicializar threads para leitura dos arquivos
			for (int i = 0; i < readers.length; i++) {
				readers[i].start();
			}

			//esperar pela finalização da leitura dos arquivos
			for (int i = 0; i < readers.length; i++)
				while (readers[i].isAlive()) {
				}

			results = new String[readers.length];
			
			//olhar o algoritmo presente no texto-base de bayes
			Pv = new double[readers.length];
			for (int i = 0; i < readers.length; i++) {
				nExamples += readers[i].count;
				results[i] = readers[i].getFilesContent() + "\n";
				writeTextFile(results[i], localdir.getCanonicalPath()
						+ "/files/" + internalClassNames[i] + ".text");
				contents += results[i];
				double v1 = readers[i].count;
				double v2 = nExamples;
				Pv[i] = v1 / v2;
			}

			if (recreateVocabulary) {
				//criar vocabulario com base no conteudo de treinamento
				vocabulary = createVocabulary(contents);

				StringBuffer s = new StringBuffer();
				Iterator<String> it = vocabulary.keySet().iterator();
				while (it.hasNext()) {
					s.append(it.next() + "\n");
				}
				writeTextFile(s.toString(), localdir.getCanonicalPath()
						+ "/files/vocabulary.text");
				System.out.println("File " + localdir.getCanonicalPath()
						+ "/files/vocabulary.text" + " successfully created.");
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
	 * Retorna um array contendo os nomes das classes (nome das pastas) de um array de pastas
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
	 * Lista os arquivos em um diretório
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
	 * Retorna a maior classe (maior posição) de um array
	 * @param t Array de double
	 * @return maior posição do array (não o maior valor!)
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
	
	private static String readFile(String filename){
		Reader reader;		
		reader = new Reader("", filename, false);
		reader.start();
		while (reader.isAlive()) {
		}
		return reader.filesContents;
	}

	private static void classify(String[] input) {
		StringBuffer stringBuffer = new StringBuffer();
		System.out.println("--------------------------------------------------\nStarting classification...");
		//para cada input, que deve ser um diretório
		for (int i = 0; i < input.length; i++) {
			System.out.println("Classifying files from folder " + internalClassNames[i]);
			// listar os arquivos na pasta output de cada classe
			String[] files = dirlist(input[i] + "/output/");			
			for (int j = 0; j < files.length; j++) {			
				// classificar o conteúdo de cada arquivo
				stringBuffer.append(classify(readFile(files[j]), files[j]) + "\n");				
			}			
		}
		//workaround: por alguma razão, estava zerando quando a divisão era feita "in-line"
		double p1 = (classificationSamples-matchCount);
		double p2 = classificationSamples;
		double d = p1/p2;
		stringBuffer.append("Global error: " + Double.toString(d));
		
		try {
			writeTextFile(stringBuffer.toString(), localdir.getCanonicalPath()
						+ "/files/classification.text");			
			System.out.println("--------------------------------------------------\nClassification finalized. File " + localdir.getCanonicalPath()
					+ "/files/classification.text sucessfully created.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String classify(String input, String name) {
		// HashMap<String, Double> PwKv1 = getProbabilities("alt.atheism");
		
		//contador de amostras de classificação
		classificationSamples++;
		
		StringTokenizer tokenizer;
		
		tokenizer = new StringTokenizer(input);

		//array para armazenar os resultados da classificação
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
			className = f.getParentFile().getParentFile().getName();
		
		match = (className.compareTo(internalClassNames[classMax]) == 0);
		//contador de acertos
		if (match)
			matchCount++;
		return(" {" + match + "}" + name + " = "
				+ internalClassNames[classMax]);

	}

	public static void main(String[] args) {

		try {
			//nome do arquivo de vocabulário
			VocabularyFileName = localdir.getCanonicalPath()
					+ "/files/vocabulary.text";

			// String[] testDirs = {
			// localdir.getCanonicalPath()
			// + "/files/20news-bydate-test/rec.autos",
			// localdir.getCanonicalPath()
			// + "/files/20news-bydate-test/talk.religion.misc",
			// localdir.getCanonicalPath()
			// + "/files/20news-bydate-test/soc.religion.christian" };
			// String[] trainDirs = {
			// localdir.getCanonicalPath()
			// + "/files/20news-bydate-train/rec.autos",
			// localdir.getCanonicalPath()
			// + "/files/20news-bydate-train/talk.religion.misc",
			// localdir.getCanonicalPath()
			// + "/files/20news-bydate-train/soc.religion.christian" };

			String[] testDirs = dirlist(localdir.getCanonicalPath()
					+ "/files/20news-bydate-test/");
			String[] trainDirs = dirlist(localdir.getCanonicalPath()
					+ "/files/20news-bydate-train/");

			classTexts = prepare(trainDirs, testDirs, true);
			
			train();

			classify(testDirs);
			//System.out.println(classify(readFile("/home/paulojr/workspace/NaiveBayesClassifier/files/20news-bydate-test/comp.sys.ibm.pc.hardware/output/60772"), "teste"));
			//System.out.println(classify("almish god prayers", "teste"));
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
