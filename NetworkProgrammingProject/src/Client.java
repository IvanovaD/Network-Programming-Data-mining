
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;



public class Client {
	public static final int SERVER_PORT = 4444;
	
; 
	public static void main(String[] args) {
		System.out.println("Client started ...");
		String host= "localhost";
	    File fileName = new File(".//logs_BCS37_20181103_UTF-8.csv");
		
		try {
			Socket socket= new Socket(host, SERVER_PORT);
			BufferedReader rcvMsg= new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintStream sendMsg= new PrintStream(socket.getOutputStream());
			BufferedReader fromFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			
			String fileInput;
			System.out.println("Please, enter minimal support");
			fileInput = stdIn.readLine(); 
			stdIn.close();
			sendMsg.println(fileInput);
			
			//sending the log file
			while ((fileInput = fromFile.readLine())!=null) {
				sendMsg.println(fileInput);
			}
			sendMsg.println("end");
			sendMsg.flush();
			  
			//the information from the server
			while (!((fileInput = rcvMsg.readLine()).equals("end"))) {
				System.out.println(fileInput);
			}
			
			
		   fromFile.close();
			rcvMsg.close();
			fromFile.close();
	        sendMsg.close();
			socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

 		
