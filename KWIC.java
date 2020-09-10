package KWIC;
import java.io.*;
import java.util.*;
public class KWIC {
	public static boolean isalpha(String word) {
		return word.matches("[a-zA-Z]+");
	}
	//This function take a BufferedReader and return a list of ignore words.
	public static ArrayList <String> read_ignore_word(BufferedReader reader) throws IOException{
		ArrayList <String> ignore_list = new ArrayList <String> ();
		String c;
		while ((c = reader.readLine())!= null) {
			if (c.equals("")) {
				System.err.println("Invalid ignore word entered");
				System.exit(1);
			}
			if (c.equals( "::")) {
				break;
			}
			if(c.split(" ").length != 1) {
				System.err.println("several ignore word in one line");
				System.exit(1);
			}
			if (!isalpha(c.toLowerCase())) {
				System.err.println("Invalid char in word");
				System.exit(1);
			}
			ignore_list.add(c.toLowerCase());
		}
		if (ignore_list.size() > 50 ) {
			System.err.println("Too many ignore word");
			System.exit(1);
		}
		return ignore_list;
	}
	
	//This function take a BufferedReader which ignore list has been read,
	//It will return a 2D list of string, the component is word
	public static ArrayList <List<String>> read_context_file(BufferedReader reader) throws IOException{
		ArrayList<List<String>> context = new ArrayList<List<String>> ();
		String c;
		while ((c = reader.readLine()) != null) {
			if (c.equals("")) {
				break;
			}
			String temp [] = c.split(" ");
			if (temp.length > 14) {
				System.err.println("Too many words in one line");
				System.exit(1);
			}
			for (int i = 0; i < temp.length; i++) {
				temp[i] = temp[i].toLowerCase();
				if (!isalpha(temp[i].toLowerCase())) {
					System.err.println("Invalid char in word");
					System.exit(1);
				}
			}
			List<String> ntemp = Arrays.asList(temp);
			context.add(ntemp);
		}
		if (context.size() > 199) {
			System.err.println("Too many titles");
			System.exit(1);
		}
		return context;
	}
	
	//This function should take a 2D list of context and return a word list
	//It should also modified the index list to encrypted x + y * length mode
	public static List <String> key_list(ArrayList<List <String>> context, ArrayList<List<Integer>> index){
		List <String> word_list = new ArrayList<String> ();
		//This list is used to store the ending word in each sentence.
		//The index is used later to compute encrypted number;
		List <Integer> ending_index = new ArrayList<Integer>();
		int count  = 0;
		for (int i = 0; i < context.size();i++) {
			for (int j = 0; j < context.get(i).size(); j++) {
				//Add each word one by one 
				word_list.add(context.get(i).get(j));
				count++;
			}
			ending_index.add(count);
		}
		//reset count here;
		count = 0;
		int total = word_list.size();
		//The following part will produce an encrypted index for word_list
		//This encrypted method will tell which line the word is in.
		for (int i = 0; i < total;i++) {
			List<Integer> temp = new ArrayList<Integer>();
			temp.add(i + total * count);
			index.add(temp);
			if (i == ending_index.get(count)) {
				count++;
			}
		}
		return word_list;
	}
	
	//This function should take a word list and an encrypted index 
	//During this function, some encrypted index should be deleted.
	//It should modifies key word list, sanitize for repetition and ignore_words and return word number before sanitize
	
	public static Integer sanitize (List<String> ignore_list,List<String> word_list,ArrayList<List<Integer>> index_list) {
		//Here to avoid index confusion after removing, word_checking should start from very end.
		int total = word_list.size();
		for (int i = total -1; i > -1 ;i--) {
			boolean removed_flag = false;
			//First inner loop will go over ignore_list to check if delete is need.
			//If in ignore_list, then delete the row in index_list
			for (int j = 0; j < ignore_list.size(); j++) {
				if (word_list.get(i).equals( ignore_list.get(j))) {
					word_list.remove(i);
					index_list.remove(i);
					removed_flag = true;
					break;
				}
			}
			//If word has been removed in the first loop, then repetition check is redundant. If so, just continue.
			if(removed_flag) {
				continue;
			}
			//Second inner loop will go over word_list to see if there is repetition
			//Again go from the end the start, if there is repetition use add all to add to the previous one
			//After adding to the previous one, delete the later one.
			for (int j = i - 1 ; j > -1 ; j--) {
				if (word_list.get(i).equals(word_list.get(j))) {
					word_list.remove(i);
					index_list.get(j).addAll(index_list.get(i));
					index_list.remove(i);
					break;
				}
			}
		}
		return total;
	}
	
	//This function takes key_word list and a index_list, returns void
	//It will sort the key_word list and change index_list accordingly
	public static void sorting(List<String> key_list, ArrayList<List<Integer>> index_list) {
		//Basic idea here is bubble sort
		String temp;
		List<Integer> temp_index = new ArrayList<Integer>();
		for (int i = 0; i < key_list.size(); i++) {
			for (int j = i + 1; j < key_list.size(); j++) {
				if (key_list.get(i).compareTo(key_list.get(j)) > 0) {
					//This part will change keyword_list 
					temp = key_list.get(i);
					key_list.set(i, key_list.get(j));
					key_list.set(j,temp);
					//The following part will change index_list
					temp_index = index_list.get(i);
					index_list.set(i,index_list.get(j));
					index_list.set(j,temp_index);
					
				}
			}
		}
	}
	
	
	//This function serves as print our method. Print out each word in proper cases.
	public static void print_out(ArrayList<List<String>> context,List <String> keyword_list, ArrayList<List<Integer>> index_list,Integer total) {
		for (int i  = 0 ; i < keyword_list.size(); i++) {
			Integer prev_row = -1;
			Integer prev_k = -1;
			
			for (int j = 0; j < index_list.get(i).size(); j++) {
				Integer temp = index_list.get(i).get(j);
				Integer row = temp / total;
				
				Integer count = 0;
				
				for (int k = 0; k < context.get(row).size();k++) {
					String tword = context.get(row).get(k);
					
					if (tword.equals(keyword_list.get(i))){
						if (count == 0) {
							if (prev_row != row) {
								System.out.print(tword.toUpperCase() + " ");
								prev_k = k;
								count = 1;
							}
							else if (prev_row == row && k > prev_k) {
								System.out.print(tword.toUpperCase() + " ");
								prev_k = k;
								count = 1;
							}else {
								System.out.print(tword+ " ");
							}
						}else {
							System.out.print(tword+ " ");
						}
					}else {
						System.out.print(tword+ " ");
					}
				}
				prev_row = row;
				System.out.println();
			}
		}
	}
	
	public static void main(String[] args) throws IOException{
		new BufferedReader(new InputStreamReader(System.in));
		//The following part checks if file name is valid and severs as file reader 
		//FileReader in = null;
		//if (args.length == 1) {
		//	try {
		//		in = new FileReader(args[0]);
		//	}catch (FileNotFoundException a) {
		//	      System.err.println("No such file");
		//	      System.exit(1);
		//	}
		//}else {
		//	System.err.println("Invalid argument amount");
		//	System.exit(1);
		//}
		//Read and store file context, i.e. ignore word list and Sentences
		//BufferedReader reader = new BufferedReader(in);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		List <String> ignore_list = read_ignore_word(reader);
		ArrayList<List<String>> context = read_context_file(reader);
		//Create a new integer list here for use
		ArrayList <List<Integer>> index_list = new ArrayList<List<Integer>>();
		List <String> keyword_list = key_list(context, index_list);
		Integer total = sanitize(ignore_list,keyword_list,index_list);
		sorting(keyword_list,index_list);
		print_out(context,keyword_list,index_list,total);
	}
	

}
