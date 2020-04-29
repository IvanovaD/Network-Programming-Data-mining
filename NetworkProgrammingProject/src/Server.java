

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import network.connection.AlgoNegFIN;

public class Server {
	public static final int SERVER_PORT = 4444;

	public static void main(String[] args) {
		System.out.println("Server started ...");
		try (ServerSocket ss = new ServerSocket(SERVER_PORT);) {

			while (true) {
				Socket s = ss.accept();
				ServerThread t = new ServerThread(s);
				t.start();
			}
			

		} catch (IOException e) {
			System.out.println("There is a problem with the server socket");
			e.printStackTrace();
		}
	
	}
}

class ServerThread extends Thread {
	Socket socket;
	BufferedReader in;
	PrintStream out;
	String inputFile;
	String outputFile;

	public ServerThread(Socket s) throws IOException {
		socket = s;

		outputFile = ".//output.txt";
		inputFile = ".//input.txt";
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintStream(socket.getOutputStream());
	}

	public Map<Integer, String> convertTEXT(String charset, String input) throws IOException {
		// This map will be used to store mapping from item id (key) to words (value).
		Map<Integer, String> mapItemsIDToWords = null;
		mapItemsIDToWords = new HashMap<Integer, String>();

		// A map that store the corresponding Item ID for each word
		// An entry in the map is :
		// key = a word
		// value = Integer (item id)
		Map<String, Integer> mapWordsToItemIDs = new HashMap<String, Integer>();

		// object for writing the output file
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Tmp.txt"), charset));

		// Now we will read the input file
		BufferedReader myInput = null;
		try {
			// Create some objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin, charset));

			// Create a string builder to store the current sentence
			StringBuilder currentSentence = new StringBuilder();

			// Variable to be used to assign item ids (integers) to words
			int nextItemID = 1;

			boolean isFirstWordOfSentence;

			// For each line in the input file
			String thisLine;
			myInput.readLine();
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is too short (e.g emptylines), skip it
				if (thisLine.length() < 1) {
					continue;
				}
				 isFirstWordOfSentence = true;
				// split the line into words
				String words[] = thisLine.split(",");

			// for each word
				for (int i = 2; i < words.length; i++) {

					if (words[i].length() != 0) {

						// Convert the word to an item
						Integer item = mapWordsToItemIDs.get(words[i]);
						if (item == null) {
							// Give a new ID to this item
							item = nextItemID++;
							// Remember the ID
							mapWordsToItemIDs.put(words[i], item);
							if (mapItemsIDToWords != null) {
								mapItemsIDToWords.put(item, words[i]);
							}
						}

						// First we will save the word in the output file
						// If it is not the first word we will add a space.
						if (isFirstWordOfSentence) {
							isFirstWordOfSentence = false;
						} else {
							currentSentence.append(" ");
						}
						currentSentence.append(item);
					}
				}
				// write the current sentence to the file
				writer.write(currentSentence.toString());
				writer.newLine();
				currentSentence.setLength(0);

			}

			// close output file
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		
		return mapItemsIDToWords;	

	}

	public void sortFile(String file) {
		FileInputStream fin;
		try {

			fin = new FileInputStream(new File(file));
			BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Tmp2.txt"), "UTF-8"));

			String sentence;
			Integer value;
			List<Integer> line = new ArrayList<>();
			while ((sentence = myInput.readLine()) != null) {

				String split[] = sentence.split(" ");
				for (String s : split) {
					value = Integer.valueOf(s);
					line.add(value);
				}
				line.sort(null);
				
				for (Integer element : line) {

					writer.write(Integer.toString(element));
					
					if (line.indexOf(element) != line.size() - 1) {
						writer.write(" ");
					}
				}
				writer.newLine();
				line.clear();
			}

			writer.flush();
			myInput.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	void convertSPMF( Map<Integer, String> mapItemsIDToWords)
	{
        String fileInput;
		String value;
		BufferedReader buffer;
		BufferedWriter writer;
		try {
			buffer = new BufferedReader(new InputStreamReader(new FileInputStream(new File("output.txt")), "UTF-8"));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("forUser.txt"), "UTF-8"));
			
			while ((fileInput = buffer.readLine()) != null) {
				String split[] = fileInput.split(" #SUP:");
				String subsplit[]=(split[0]).split(" ");
				for(String s : subsplit)
				{	value = mapItemsIDToWords.get(Integer.parseInt(s));
				    writer.write(value+" ");   
				}
			writer.write("SUP:");
			writer.write(split[1]);
			writer.newLine();
			}
		
		writer.flush();
		writer.close();
		buffer.close();
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void run() {
		String msgreceive;
		String minsup;

		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(inputFile))));) {
			minsup = in.readLine();
			msgreceive = in.readLine();

			while (!(msgreceive = in.readLine()).equals("end")) {
				writer.write(msgreceive);
				writer.newLine();

			}
			writer.flush();

			Map<Integer, String> mapItemsIDToWords = convertTEXT("UTF-8", inputFile);
			sortFile("Tmp.txt");

			String input = "Tmp2.txt";

			double doublePrim = Double.parseDouble(minsup);
		
			// Applying the algorithm
			AlgoNegFIN algorithm = new AlgoNegFIN();
			
			algorithm.runAlgorithm(input, doublePrim, outputFile);
			algorithm.printStats();
			
			
			convertSPMF(mapItemsIDToWords);
			
			
			BufferedReader buffer = new BufferedReader(	new InputStreamReader(new FileInputStream(new File(".//forUser.txt")), "UTF-8"));
			String fileInput;

			while ((fileInput = buffer.readLine()) != null) {			
				out.println(fileInput);
			}
			out.println("end");
			out.flush();
			out.close();
			in.close();
			writer.close();
			buffer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*public static String fileToPath(String filename) throws
	 UnsupportedEncodingException{ URL url =
	 MainTestFIN.class.getResource(filename); return
	 java.net.URLDecoder.decode(url.getPath(),"UTF-8"); }
	 **/

}
