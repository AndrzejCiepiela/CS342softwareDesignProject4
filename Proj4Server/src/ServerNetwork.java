import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class ServerNetwork {
	
	private ArrayList<Serializable> activeConnections = new ArrayList<>(); // used to keep track of active players and their ID's
	private ConnThread connthread = new ConnThread();
	private Consumer<Serializable> callback;
	private ArrayList<myThread> clients = new ArrayList<myThread>();
	
	public ServerNetwork(Consumer<Serializable> callback) {
		this.callback = callback;
		connthread.setDaemon(true);
	}
	
	// start the thread connthread
	public void startConn() throws Exception{
		connthread.start();
	}
	
	// send messages
	public void send(Serializable data) throws Exception{
		
		for(int i = 0; i < clients.size(); i++) {
			try {
				clients.get(i).out.writeObject(data);
		
			}catch(Exception e) {
				System.out.println("One of the clients closed");
			}
		}
	}
	
	// close the socket in thread connthread
	public void closeConn() throws Exception{
		connthread.socket.close();
	}
	
	// convert the arraylist of active users to a string to send to server.
	public Serializable convertToString(ArrayList<Serializable> arr)  {
		Serializable result = "";
		for(int i =0; i<arr.size(); i++) {
			result = result + (arr.get(i) + " ");
		}

		return result;

	}
	
	public void sendtoX(int clientID, ObjectOutputStream out) throws Exception {

		Serializable message = "ID " + clientID;
		if(clients.get(clientID-1) == null) {
			System.out.println("clients is empty\n");
		}
		out.writeObject(message);
	}
	
	// compare the choices
	public String compare(String data1, String data2) {
		
		String message = "";
		
		switch(data1) {
			
		case "Paper":
			if(data2.equals("Rock")) {
				message = "Paper beats Rock";
			}
			else if(data2.equals("Spock")) {
				message = "Paper beats Spock";
			}
			else if(data2.equals("Scissors")) {
				message = "Scissors beats Paper";
			}
			else if(data2.equals("Lizard")) {
				message = "Lizard beats Paper";
			}
			else if(data2.contentEquals("Paper")) {
				message = "Both players played Paper, the game is a draw";
			}
			break;
			
		case "Rock":
			if(data2.equals("Scissors")) {
				message = "Rock beats Scissors";
			}
			else if(data2.equals("Lizard")) {
				message = "Rock beats Lizard";
			}
			else if(data2.equals("Paper")) {
				message = "Paper beats Rock";
			}
			else if(data2.equals("Spock")) {
				message = "Spock beats Rock";
			}
			else if(data2.contentEquals("Rock")) {
				message = "Both players played Rock, the game is a draw";
			}
			break;
			
		case "Scissors":
			if(data2.equals("Paper")) {
				message = "Scissors beats Paper";
			}
			else if(data2.equals("Lizard")) {
				message = "Scissors beats Lizard";
			}
			else if(data2.equals("Rock")) {
				message = "Rock beats Scissors";
			}
			else if(data2.equals("Spock")) {
				message = "Spock beats Scissors";
			}
			else if(data2.contentEquals("Scissors")) {
				message = "Both players played Scissors, the game is a draw";
			}
			break;
			
		case "Lizard":
			if(data2.equals("Paper")) {
				message = "Lizard beats Paper";
			}
			else if(data2.equals("Spock")) {
				message = "Lizard beats Spock";
			}
			else if(data2.equals("Scissors")) {
				message = "Scissors beats Lizard";
			}
			else if(data2.equals("Rock")) {
				message = "Rock beats Lizard";
			}
			else if(data2.contentEquals("Lizard")) {
				message = "Both players played Lizard, the game is a draw";
			}
			break;
			
		case "Spock":
			if(data2.equals("Rock")) {
				message = "Spock beats Rock";
			}
			else if(data2.equals("Scissors")) {
				message = "Spock beats Scissors";
			}
			else if(data2.equals("Paper")) {
				message = "Paper beats Spock";
			}
			else if(data2.equals("Lizard")) {
				message = "Lizard beats Spock";
			}
			else if(data2.contentEquals("Spock")) {
				message = "Both players played Spock, the game is a draw";
			}
			break;
		}
		
		return message;
	}
	
	// abstract methods
	abstract protected boolean isServer();
	abstract protected String getIP();
	abstract protected int getPort();
	
	class ConnThread extends Thread{
		private Socket socket;
		private ObjectOutputStream out;
		private int clientID = 1;
		
		public void run() {
			try{
				
				// if server, listen for connections from clients
				ServerSocket server = new ServerSocket(getPort());
				
				// server start message
				callback.accept("Server created \nNow waiting for clients...");
				
				while(true) {
					myThread t = new myThread(server.accept(), clientID);
					t.start();
					clients.add(t);
					callback.accept("Player " + clientID + " joined");
					clientID++;
				}
			}
			catch(Exception e) {
				callback.accept("connection Closed");
			}
		}
	}
	
	// separate thread for the client
	class myThread extends Thread {
		
		private Socket socket;
		private ObjectOutputStream out;
		private int myID;
		private int opponentID = 0;
		private boolean lookingForGame = false;
		private boolean isChallenged = false;
		private boolean inGame = false;
		private boolean isHost = false;
		private String dataOne;
		private String dataTwo;
		private boolean madeChoice;
		
		myThread(Socket s, int i) {
			this.socket = s;
			this.myID = i;
		}
		
		public void run() {
			try(
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){
				
				this.out = out;
				socket.setTcpNoDelay(true);
				
				//sets the ID-1 index of the player to true, indicating they are connected
				activeConnections.add("true");
				//inform the client of their id
				sendtoX(myID,out);
				send(convertToString(activeConnections));
				System.out.println("\nSent activeConnections with a size of: " + activeConnections.size());
				
				while(true) {
					
					Serializable data = (Serializable) in.readObject();
					
					// when player wants to return to the lobby
					if(data.toString().intern() == "quit") {
						inGame = false;
						lookingForGame = false;
						isHost = false;
						isChallenged = false;
					}
					
					// if this client is currently playing a game, send choice to server
					if(inGame) {
						
						// show server what client picked
						callback.accept("Player " + myID + " picked " + data);
						
						// store the two player's choices
						
						// if player making the choice is the challenger
						if(isHost) {
							madeChoice = true;
							dataOne = data.toString();
							clients.get(opponentID - 1).dataOne = data.toString();
							
							// check and see if both players have made a choice
							if(madeChoice == true && clients.get(opponentID - 1).madeChoice == true) {
								
								String winMessage = compare(dataOne, dataTwo);
								
								// show players what each person played
								out.writeObject("Player " + myID + " played " + dataOne + ", Player " + opponentID + " played " + dataTwo + "\n" + winMessage);
								clients.get(opponentID - 1).out.writeObject("Player " + myID + " played " + dataOne + ", Player " + opponentID + " played " + dataTwo + "\n" + winMessage);
								
								// if game was a draw
								if(winMessage.equals("Both players played " + dataOne + ", the game is a draw")) {
									
									out.writeObject("There must be a winner, pick again");
									clients.get(opponentID - 1).out.writeObject("There must be a winner, pick again");
								}
								else {
									// send players message that the game is over
									out.writeObject("Thanks for playing, press quit to return to lobby");
									clients.get(opponentID - 1).out.writeObject("Thanks for playing, press quit to return to lobby");
								}
								
								callback.accept(winMessage);
								
								// reset
								madeChoice = false;
								clients.get(opponentID - 1).madeChoice = false;
							}
						}
						
						// else other player is making their choice
						else {
							madeChoice = true;
							dataTwo = data.toString();
							clients.get(opponentID - 1).dataTwo = data.toString();
							
							// check and see if both players have made a choice
							if(madeChoice == true && clients.get(opponentID - 1).madeChoice == true) {
								
								String winMessage = compare(dataOne, dataTwo);
								
								// show players what each person played
								out.writeObject("Player " + opponentID + " played " + dataOne + ", Player " + myID + " played " + dataTwo + "\n" + winMessage);
								clients.get(opponentID - 1).out.writeObject("Player " + opponentID + " played " + dataOne + ", Player " + myID + " played " + dataTwo + "\n" + winMessage);
								
								// if game was a draw
								if(winMessage.equals("Both players played " + dataOne + ", the game is a draw")) {
									
									out.writeObject("There must be a winner, pick again");
									clients.get(opponentID - 1).out.writeObject("There must be a winner, pick again");
								}
								else {
									// send players message that the game is over
									out.writeObject("Thanks for playing, press quit to return to lobby");
									clients.get(opponentID - 1).out.writeObject("Thanks for playing, press quit to return to lobby");
								}
								
								callback.accept(winMessage);
								
								// reset
								madeChoice = false;
								clients.get(opponentID - 1).madeChoice = false;
							}
						}
						
					}
					
					// run this block of code if not in a game
					if(!inGame && data.toString().intern() != "quit") {
						
						// if client is challenging a player
						if(lookingForGame == false && !isChallenged) {
							
							// get ID of the opponent
							int tmpID = Integer.parseInt(data.toString());
							opponentID = clients.get(tmpID - 1).myID;
							
							// this client is now waiting for response from opponent
							lookingForGame = true;
							isHost = true;
							
							// send message to server that a client has challenged someone
							callback.accept("Player " + myID + " is challenging Player " + opponentID);
							
							// send message to challenger
							out.writeObject("joined game, waiting for opponent...");
							
							// have server send message to opponent that they are being challenged
							clients.get(opponentID - 1).out.writeObject("Your being challenged");
							clients.get(opponentID - 1).isChallenged = true;
							clients.get(opponentID - 1).opponentID = myID;
						}
						
						// if client is waiting for response from opponent
						if(lookingForGame == true) {
							
							// if answer is yes
							inGame = true;
						}
						
						// if client got challenged
						if(isChallenged) {
							
							// if answer is yes
							if(data.toString().intern() == "accepted") {
								inGame = true;
								
								// send challenger message that their challenge was accepted
								clients.get(opponentID - 1).out.writeObject("Opponent has joined your game, begin playing");
								
								// send message to player that was challenged
								out.writeObject("You have joined game, begin playing");
								
								callback.accept("Player " + opponentID + " is now playing Player " + myID);
							}
						}
						
					}
					
				}
			}
			catch(Exception e) {
				// send an updated list to the remaining players connected,
				// one without this player
				try {
					activeConnections.set(myID,"false");
				}catch(Exception f) {
					System.out.println("Socket was already closed");
				}

				try {
					send(convertToString(activeConnections)); }
				catch(Exception f) {
					f.printStackTrace();
				}
				callback.accept("Player " + myID + " disconnected");
			}
		}
	}
}
