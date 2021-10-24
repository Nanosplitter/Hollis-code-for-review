//Programmer: Brandon Hollis COSC 439/522, F '21
//Multi-threaded Server program
//File name: bha_TCPServerMT.java
//When you run this program, you must give the service port
//number as a command line argument with the correct identifiers. For example,
//java TCPServerMT 22222
//Class description: The server side of this project is responsible for sending a lot of data back to the client such as:
//The initial chat logs as they connect, a live broadcast of what other clients are sending, how many messages the
//server has received from them and how long they were connected to the server for.

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class bha_TCPServerMT {
	private static ServerSocket servSock;
	private static ArrayList<Socket> list = new ArrayList<>();
	private static File chatLogs = new File("bha_chat.txt");

	public static void main(String[] args) {
		System.out.println("Opening port...\n");
		int port = 20500;
		for (int i = 0; i < args.length; i+=2) {
			if (args[i].equals("-p")) {
				port = Integer.parseInt(args[i + 1]);
			}
			else {
				System.out.println("Invalid input");
				System.exit(1);
			}
		}

		try {
			// Create a server object
			servSock = new ServerSocket(port);
		}

		catch (IOException e) {
			System.out.println("Unable to attach to port!");
			System.exit(1);
		}
		do {
			run();
		} while (true);

	}

	private static void run() {
		Socket link = null;
		try {

			// Put the server into a waiting state
			link = servSock.accept();
			
			
			//Make sure chat logs are created and ready to record or deleted if no one is using the server
			checkFile();
			
			// print local host name
			String host = InetAddress.getLocalHost().getHostName();
			System.out.println("Client has estabished a connection to " + host);

			// Create a thread to handle this connection
			ClientHandler handler = new ClientHandler(link);
			list.add(link);
			// start serving this connection
			handler.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	//Once the client disconnects this method removes the client from the list of "active clients"
	public static void removeClient(Socket client) {
		int indexOfSenderClient = list.indexOf(client);
		list.remove(indexOfSenderClient);
	}
	//Checks to make sure file is either created when it should be or deleted when it should be.
	public static void checkFile() {
		if(list.size() == 0 && !chatLogs.exists()) {
			try {
				chatLogs.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(list.size() == 0 && chatLogs.exists()) {
			chatLogs.delete();
		}
	}
	//This method is in charge of sending a message to all clients except to the client it belongs to
	public static void broadcast(Socket sender, String message) {
		int indexOfSenderClient = list.indexOf(sender);
		for(int i = 0; i < list.size(); i++) {
			if(i != indexOfSenderClient) {
				try {
					new PrintWriter(list.get(i).getOutputStream(), true).println(message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	//Gets chatLogs so new users can see the previous messages in the chat
	public static void getChatLogs(Socket client) {
		
		try {
			Scanner logReader = new Scanner(chatLogs);
			PrintWriter getLogs = new PrintWriter(client.getOutputStream(), true);
			while(logReader.hasNext()) {
				String oldChat = logReader.nextLine();
				getLogs.println(oldChat);
			}
			logReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	//Used to write messages to the log in a synchronized matter
	public synchronized static void writeToLog(String message) {
		try {
			BufferedWriter writeToLogs = new BufferedWriter(new FileWriter(chatLogs, true));
			writeToLogs.write(message + "\n");
			writeToLogs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

class ClientHandler extends Thread {
	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private String user;
	
	public ClientHandler(Socket s) {

//set up the socket
		client = s;
		try {

//Set up input and output streams for socket
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//overwrite the method 'run' of the Runnable interface

//this method is called automatically when a client thread starts.
	public void run() {

//Receive and process the incoming data 
		
		int numMessages = 0;
		try {
			
			user = in.readLine();
	
			long startTime = System.currentTimeMillis();
			bha_TCPServerMT.broadcast(client, user + " has entered the server");
			bha_TCPServerMT.writeToLog(user + " has entered the server");
			bha_TCPServerMT.getChatLogs(client);
			
			String message = in.readLine();
			while (!message.equals("DONE")) {
				System.out.println(user + ": " + message);
				bha_TCPServerMT.broadcast(client, user + ": " + message);
				bha_TCPServerMT.writeToLog(user + ": " + message);
				numMessages++;
				message = in.readLine();
			}

//Send a report back and close the connection
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime);
			long milli = duration;
			long seconds = milli / 1000;
			long minutes = seconds / 60;
			long hours = minutes / 60;
			long minmod = minutes % 60;
			long secmod = seconds % 60;
			long milimod = milli % 1000;
			
			bha_TCPServerMT.removeClient(client);
			bha_TCPServerMT.broadcast(client, user + " has left the server");
			bha_TCPServerMT.writeToLog(user + " has left the server");
			bha_TCPServerMT.checkFile();
			
			out.println("Server received " + numMessages + " messages");
			out.println(String.format("%d::%d::%d::%d\n", hours, minmod, secmod, milimod));
			out.println("Done");
		} catch (IOException e) {
			//e.printStackTrace();
		} 
		
		finally {
			try {
				System.out.println("!!!!! Closing connection... !!!!!");
				client.close();
			} catch (IOException e) {
				System.out.println("Unable to disconnect!");
				System.exit(1);
			}
		}

	}

}
