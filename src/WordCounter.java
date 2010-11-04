import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

public class WordCounter {

	private HashMap<String, Integer> map = new HashMap<String, Integer>();
	private String text;
	
	public WordCounter(String input){
		text = input;
		getDistinctWordsCount();
	}

	public int getDistinctWordsCount() {
		StringTokenizer tokenizer = new StringTokenizer(text);

		while (tokenizer.hasMoreElements()) {
			String word = tokenizer.nextToken().toLowerCase();

			if (map.containsKey(word)) {
				int count = map.get(word);
				map.put(word, count + 1);
			} else {
				map.put(word, 1);
			}
		}
		return map.keySet().size();
	}

	public int getWordsCount() {
		StringTokenizer tokenizer = new StringTokenizer(text);
		int count = 0;
		while (tokenizer.hasMoreElements()) {			
			count++;
			tokenizer.nextToken();
		}
		return count;
	}

	public int getWordCountInText(String word) {
		if (map.containsKey(word)) {
			return map.get(word);
		} else {
			return 0;
		}
	}
}