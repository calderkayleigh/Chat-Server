package chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Kayleigh Calder
 */
public class ChatServer extends ChatWindow
{

	private ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
	public static String chatServerName = "Chat Server";
	public static int portNumber = 2113;

	public ChatServer(){
		super();
		this.setTitle(chatServerName);
		this.setLocation(80,80);

		try {
			// Create a listening service for connections
			// at the designated port number.
			ServerSocket srv = new ServerSocket(portNumber);

			while (true)
			{
				// The method accept() blocks until a client connects.
				printMsg("Waiting for a connection");
				Socket socket = srv.accept();

				if(socket != null)
				{
					ClientHandler handler = new ClientHandler(socket);
					Thread newThread = new Thread(handler);
					newThread.start();
					clientHandlers.add(handler);
				}
			}

		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}

	/** This innter class handles communication to/from one client. */
	class ClientHandler implements Runnable{
		private PrintWriter writer;
		private BufferedReader reader;
		private String username;

		public ClientHandler(Socket socket)
		{
			try
			{
				InetAddress serverIP = socket.getInetAddress();
				printMsg("Connection made to " + serverIP);
				writer = new PrintWriter(socket.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				username = reader.readLine();
				printMsg(username + "has joined");
				sendMsg("Hi " + username + " You have joined the classroom");
			}
			catch (IOException e)
			{
					printMsg("\nERROR:" + e.getLocalizedMessage() + "\n");
			}
		}
		public void handleConnection()
		{
			System.out.println("handle Connection");
			try
			{
				while(true)
				{
					System.out.println("handle connection");
					// read a message from the client
					sendToAllInRoom(readMsg());
				}
			}
			catch (IOException e)
			{
				printMsg("\nERROR:" + e.getLocalizedMessage() + "\n");
			}
		}

		/** Receive and display a message
		 * @return*/
		public String readMsg() throws IOException
		{
			String s = reader.readLine();
			printMsg(s);
			return s;
		}

		/** Send a string */
		public void sendMsg(String s)
		{
			writer.println(s);
		}

		//send the message to all users
		public void sendToAllInRoom (String s)
		{
			//iterate through clients and send the message
			for(int i=0; i<clientHandlers.size(); i++)
			{
				clientHandlers.get(i).sendMsg(s);
			}
		}
		public void run()
		{
			handleConnection();
		}
	}

	public static void main(String args[]){
		new ChatServer();
	}
}
