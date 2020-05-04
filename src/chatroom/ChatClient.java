package chatroom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

/**
 * Kayleigh Calder
 */
public class ChatClient extends ChatWindow
{
	// GUI Objects
	private JTextField serverTxt;
	private JTextField nameTxt;
	private JButton connectB;
	private JTextField messageTxt;
	private JButton sendB;

	//variable declarations
	private String username;
	public String chatClientName = "Chat Client";
	private String chatRoom = "Default";

	public ChatClient()
	{
		super();
		this.setTitle(chatClientName);
		printMsg("Chat Client Started.");

		//assign client a username
		Random rand = new Random();
		username = "UnknownUser" + rand.nextInt(1000);

		// GUI elements at top of window
		// Need a Panel to store several buttons/text fields
		serverTxt = new JTextField("localhost");
		serverTxt.setColumns(15);
		nameTxt = new JTextField(username);
		nameTxt.setColumns(10);
		connectB = new JButton("Connect");
		JPanel topPanel = new JPanel();
		topPanel.add(serverTxt);
		topPanel.add(nameTxt);
		topPanel.add(connectB);
		contentPane.add(topPanel, BorderLayout.NORTH);

		// GUI elements and panel at bottom of window
		messageTxt = new JTextField("");
		messageTxt.setColumns(40);
		sendB = new JButton("Send");
		JPanel botPanel = new JPanel();
		botPanel.add(messageTxt);
		botPanel.add(sendB);
		contentPane.add(botPanel, BorderLayout.SOUTH);

		// Resize window to fit all GUI components
		this.pack();

		// Setup the communicator so it will handle the connect button
		Communicator comm = new Communicator();
		connectB.addActionListener(comm);
		sendB.addActionListener(comm);

	}

	/** This inner class handles communication with the server. */
	class Communicator implements ActionListener, Runnable
	{
		private Socket socket;
		private PrintWriter writer;
		private BufferedReader reader;

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if(actionEvent.getActionCommand().compareTo("Connect") == 0)
			{
				connect();
			}
			else if(actionEvent.getActionCommand().compareTo("Send") == 0) {
				sendMsg();
			}
		}

		/** Connect to the remote server and setup input/output streams. */
		public void connect(){
			try {
				socket = new Socket(serverTxt.getText(), ChatServer.portNumber);
				InetAddress serverIP = socket.getInetAddress();
				printMsg("Connection made to " + serverIP);
				writer = new PrintWriter(socket.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				//print the username
				username = nameTxt.getText();
				writer.println(username);
				readMsg();

				//create a new thread
				Thread newThread = new Thread(this);
				newThread.start();

			}
			catch(IOException e) {
				printMsg("\nERROR:" + e.getLocalizedMessage() + "\n");
			}
		}
		/** Receive and display a message */
		public void readMsg() throws IOException {
			String s = reader.readLine();
			printMsg(s);
		}

		/** Send a string */
		public void sendMsg()
		{
			//declare variables
			String message;

			//if text is blank, do next send the message
			if(messageTxt.getText().length() == 0)
			{
				return;
			}

			/**Check to see if the username has been updated*/

			char[] messageArray = messageTxt.getText().toCharArray();
			char[] updateUsernameMessage = "/name".toCharArray();
			boolean updateUsername = true;

			//cases where the username is not updated
			for(int i = 0; i<messageArray.length; i++)
			{
				if(updateUsernameMessage.length> messageArray.length || (i<updateUsernameMessage.length && messageArray[i] != updateUsernameMessage[i]))
				{
					updateUsername = false;
				}

			}

			//send message and update username
			if(updateUsername)
			{
				//placeholder for the new username
				String updatedUsername = "";

				//iterate through the message
				for(int i = updateUsernameMessage.length; i< messageArray.length; i++)
				{
					if(messageArray[i] != ' ')
					{
						updatedUsername += messageArray[i];
					}
				}
				//change the username if a new username is provided
				if(updatedUsername.length() != 0)
				{
					//confirmation message
					message = username + " is changing their name to " + updatedUsername;
					//write confirmation message
					writer.println(message);
					//update the username for future messages
					username = updatedUsername;
				}
				else
				{
					printMsg("Error");
				}
			}

			//send message without updating username
			else
			{
				//text sent
				message = "[" +chatRoom +"]" + username + " " +messageTxt.getText();
				//write the text
				writer.println(message);
			}
			messageTxt.setText(null);

		}
		@Override
		public void run()
		{
			try
			{
				while (true)
				{
					readMsg();
				}
			}
			catch(IOException e)
			{
				printMsg("Server disconnected");
			}
		}

	}


	public static void main(String args[]){
		new ChatClient();
	}

}

