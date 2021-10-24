//Programmer: Brandon Hollis COSC 439/522, F '21
//Multi-threaded Client program
//File name: TCPClientMT.java
//When you run this program, you must give both the host name and
//the service port number as command line arguments with the correct identifiers. For example,
//java bha_TCPClientMT -h localhost -p 22222
//Class description: The client side of this project is responsible for getting the user details like name, and messages
//and creating a new thread to send those messages through as a new client so that multiple clients can engage
//with the system simultaneously

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class bha_TCPClientMT {
	private static InetAddress host;
	private static String username = "";
	private static int port = 20500;

	public static void main(String[] args) {
		int check = 0;
		Scanner kb = new Scanner(System.in);
		
		//Command line arguments
		for (int i = 0; i < args.length; i+=2) {
			if (args[i].equals("-p")) {
				port = Integer.parseInt(args[i + 1]);
			}

			else if(args[i].equals("-h")) {
				try {
					// Get server IP-address
					check = 1;
					host = InetAddress.getByName(args[i + 1]);

				} catch (UnknownHostException e) {
					System.out.println("Host ID not found!");
					System.exit(1);

				}
			}
			
			else if (args[i].equals("-u")) {
				username = args[i + 1];
			}
			else {
				System.out.println("Invalid input");
				System.exit(1);
			}
		}
		if (check == 0) {
			try {
				host = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (username.isEmpty()) {
			System.out.println("Enter a username");
			username = kb.next();
		}
		run(port);
	}

	private static void run(int port) {
		Socket link = null;
		try {
			// Establish a connection to the server
			link = new Socket(host, port);
			// Set up input and output streams for the connection
			BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
			PrintWriter out = new PrintWriter(link.getOutputStream(), true);

			// create a sender thread. This thread reads messages typed at the keyboard
			// and sends them to the server
			Sender sender = new Sender(out, username);

			// start the sender thread
			sender.start();

			// the main thread reads messages sent by the server, and displays them on the
			// screen
			String message;

			// Get data from the server and display it on the screen
			while (!(message = in.readLine()).equals("DONE"))
				System.out.println(message);

		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (NullPointerException n) {
			
		}

		finally {
			try {
				System.out.println("\n!!!!! Closing connection... !!!!!");
				link.close();
			}

			catch (IOException e) {
				System.out.println("Unable to disconnect!");
				System.exit(1);
			}
		}
	}
}

// The sender class reads messages typed at the keyboard, and sends them to the server
class Sender extends Thread {
	private String user;
	private PrintWriter out;

	public Sender(PrintWriter out, String user) {
		
		this.out = out;
		this.user = user;
	}

	// overwrite the method 'run' of the Runnable interface

	// this method is called automatically when a sender thread starts.
	public void run() {
		// Set up stream for keyboard entry
		BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in));
		String message;

		// Get data from the user and send it to the server
		try {
			out.println(user);
			do {
				System.out.print("Enter message: ");
				message = userEntry.readLine();
				out.println(message);
			} while (!message.equals("DONE"));
		} catch (IOException e) {

		}
		
	}
}
