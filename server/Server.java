package PLH512.server;

import java.io.*; 
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.net.*; 

//Server class 
public class Server  
{
	static Vector<ClientHandler> playerVector = new Vector<>(); // Vector to store active players

	static int activePlayers = 0; // Counter for active players
	static final int portToSetUpServer = 64240;
	static final int numberOfPlayers = 4;
	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException
	{
		//
		// Server setup is starting..
		//
		
		Socket s = null;
		ServerSocket ss = null;
		int serverPort = portToSetUpServer; // Port to listen to
		
		System.out.println("Atempting to start server... \n"); 
		try {
			ss = new ServerSocket(serverPort);
		} catch (IOException e) {
			System.out.println("Problem with server creation.");
			e.printStackTrace();
		}   
		System.out.println("Server up & running!! Listening on port " + serverPort + "... \n");
		
		//
		// Server setup done! Waiting for players to join..
		//
		
		boolean allPlayersJoined = false; // Flag to stop accepting incoming requests
		
		System.out.println("Waiting for " + numberOfPlayers + " players to log in.. \n");
		
		while (!allPlayersJoined)  // Loop to accept incoming requests
		{  
		    try {
				s = ss.accept();
				
				System.out.println("New client request received : " + s); 
			       
			    // Obtaining input and output streams 
			    ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
			    ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
			    
			    System.out.println("Creating a new handler for this client..."); 
			    
			    // Create a new handler object for handling this request. 
			    ClientHandler mtch = new ClientHandler(s,"client " + activePlayers, dis, dos , activePlayers);
			    // Create a new Thread with this object.
			    Thread t = new Thread(mtch);  
			    System.out.println("Adding this client to active client list \n"); 
			    // Add this client to active clients list 
			    playerVector.add(mtch); 
			    // Start the thread.
			    t.start();  
			    
			    activePlayers++; // Increment activePlayers for new client 
			    
			    if (activePlayers == numberOfPlayers) {
			    	allPlayersJoined = true;
			    	System.out.println("All players are here!! We shall start the game! \n"); 
			    }
				
			} catch (IOException e) {
				System.out.println("Exception in accepting incoming client request");
				e.printStackTrace(); // Accept the incoming request
			} 
		} 
		
		//
		// All players are here! Initializing the game...
		//
		
		Board myBoard = new Board(numberOfPlayers);
		
		// Sending playerID to the Clients
		for (ClientHandler mc : Server.playerVector) 
			sendTo(mc.getPlayerNo(), mc);
		
		// Sending number of players to the Clients
		for (ClientHandler mc : Server.playerVector) 
			sendTo(numberOfPlayers, mc);
		
		// Sending roles to players
		myBoard.initializeRoles();
		
		for (ClientHandler mc : Server.playerVector) 
			sendTo(myBoard.getRoleOf(mc.getPlayerNo()), mc);
		
		// Getting the usernames from the Clients
		for (ClientHandler mc : Server.playerVector) {
			String tmpString = (String)mc.dis.readObject();
			myBoard.setUsernames(tmpString, mc.getPlayerNo());
			System.out.println("Got the username from Client[" + mc.getPlayerNo() + "] and it's " + tmpString);
		}
		
		System.out.println("");
		
		myBoard.printOutRoles();
		
		System.out.println("");
		
		myBoard.initializeCities();
		
		myBoard.initializeInfectedDeck();
		
		myBoard.initializePlayersDeck();
		
		myBoard.initializeGameVariables();
		
		myBoard.initializePawnsPositions();
		
		myBoard.manualBuildResearchStation("Atlanta");
		
		myBoard.initializePlayersHands();
		
		myBoard.addEpidemicCards();
		
		//myBoard.addEventCards();
		
		myBoard.initialInfection();
		
		System.out.println("");
		
		
		
		//
		// Game logic starts here! Starting game..
		//
		
		myBoard.resetTalkedForThisTurn();
		
		while (myBoard.getGameEnded() == false)
		{
			System.out.println("*** This is round " + myBoard.getRound() + "." + myBoard.getWhoIsPlaying() + " ***\n");
			
			myBoard.resetCanNotOutbreak();
			myBoard.printCitiesAndCubes();
			myBoard.printRemainingCubesAndDiseaseStatus();
			myBoard.printWhoIsPlayngWithRole();
			
			myBoard.setMessageToAllClients("\n*** This is round " + myBoard.getRound() + "." + myBoard.getWhoIsPlaying() + " and " + myBoard.getUsernames(myBoard.getWhoIsPlaying()) + " is playing now *** \n");
			
			int currentlyCheckingClientNo = -1;
			
			for (ClientHandler mc : Server.playerVector) {
				
				currentlyCheckingClientNo = mc.getPlayerNo();
				
				if (currentlyCheckingClientNo != myBoard.getWhoIsPlaying()) {
					
					System.out.println(myBoard.getUsernames(currentlyCheckingClientNo) + " is thinking of a suggestion to make..");
					myBoard.setMessageToClient("Server: User" + currentlyCheckingClientNo + " please import your suggestion for this turn..", mc.getPlayerNo());
					
					myBoard.setWhoIsTalking(currentlyCheckingClientNo);
					
					sendTo(myBoard, mc);
					myBoard.resetPersonalizedMessages();
					
					//System.out.println("Trying to read suggestion");
					myBoard.setActions((String)mc.dis.readObject(), currentlyCheckingClientNo);
					//System.out.println("Suggestion has been read");
					myBoard.setTalkedForThisTurn(true, currentlyCheckingClientNo);
				}
			}
			
			for (ClientHandler mc : Server.playerVector) {
				
				currentlyCheckingClientNo = mc.getPlayerNo();
				
				if (currentlyCheckingClientNo == myBoard.getWhoIsPlaying()) {
					
					System.out.println(myBoard.getUsernames(currentlyCheckingClientNo) + " is thinking of an action to make..");
					System.out.println();
					myBoard.setMessageToClient("Server: User" + currentlyCheckingClientNo + " please import your action for this turn..", mc.getPlayerNo());
					myBoard.setWhoIsTalking(currentlyCheckingClientNo);
					
					sendTo(myBoard, mc);
					myBoard.resetAllMessages();
					
					//System.out.println("Trying to read action");
					myBoard.setActions((String)mc.dis.readObject(), currentlyCheckingClientNo);
					myBoard = readActions(myBoard.getActions(currentlyCheckingClientNo), myBoard); // this was new
					//System.out.println("Action has been read");
					myBoard.setTalkedForThisTurn(true, currentlyCheckingClientNo);
					myBoard.setRound(myBoard.getRound()+1);
				}	
			}
			
			System.out.println("\nPrinting actions of all players...");
			
			for (int i = 0 ; i < activePlayers ; i++)
				System.out.println(myBoard.getUsernames(i) + " : " + myBoard.getActions(i));
			System.out.println("");
			
			if (myBoard.checkIfWon())
				break;
			
			myBoard.drawCards(myBoard.getWhoIsPlaying(), 2);
			System.out.println("");
			
			if (!myBoard.getIsQuietNight())
				myBoard.infectCities(myBoard.getInfectionRate(),1);
			else 
				myBoard.setIsQuietNight(false);
			System.out.println("");
			
			myBoard.resetTalkedForThisTurn();
			
			if (myBoard.getWhoIsPlaying() == numberOfPlayers - 1)
				myBoard.setWhoIsPlaying(0); // Back to first player
			else
				myBoard.setWhoIsPlaying(myBoard.getWhoIsPlaying() + 1); // Next player
		}
		
		//
		// Game logic ends here!
		//
		
		System.out.println("\nGame has finished. Closing resources.. \n");
		
		if (myBoard.checkIfWon())
			System.out.println("*** Players won!! ***");
		else 
			System.out.println("*** Players lost... ***");
		
		myBoard.setGameEnded(true);
		sendToAll(myBoard);
		
		TimeUnit.SECONDS.sleep(1);
		
		s.close();
		ss.close();
		
		System.out.println("Recources closed succesfully. Goodbye! \n");
	} 
	
	// Useful functions
	
	public static Board readActions(String toRead, Board board)
	{
		String delimiterActions = "#";
		String delimiterVariables = ",";
		
		String[] actions;
		String[] variables;
		
		int actionCounter = 0;
		
		actions = toRead.split(delimiterActions);
		
		for (int i = 0 ; i < actions.length; i++)
		{
			variables = actions[i].split(delimiterVariables);
			
			if (variables[0].equals("DT"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " drives to " + variables[2]);
				board.driveTo(Integer.parseInt(variables[1]), variables[2]);
				actionCounter++;
			}
				
			else if (variables[0].equals("DF"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " takes a direct flight to " + variables[2]);
				board.directFlight(Integer.parseInt(variables[1]), variables[2]);
				actionCounter++;
			}
				
			else if (variables[0].equals("CF"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " takes a charter flight to " + variables[2]);
				board.charterFlight(Integer.parseInt(variables[1]), variables[2]);
				actionCounter++;
			}
				
			else if (variables[0].equals("SF"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " takes a shuttle flight to " + variables[2]);
				board.shuttleFlight(Integer.parseInt(variables[1]), variables[2]);
				actionCounter++;
			}
				
			else if (variables[0].equals("BRS"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " is building a Research Station to " + variables[2]);
				board.buildRS(Integer.parseInt(variables[1]), variables[2]);
				actionCounter++;
			}
			else if (variables[0].equals("RRS"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " is removing a Reseaerch Station from " + variables[2]);
				board.removeRS(Integer.parseInt(variables[1]), variables[2]);
			}
			else if (variables[0].equals("TD"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " is treating the " + variables[3] + " disease from " + variables[2]);
				board.treatDisease(Integer.parseInt(variables[1]), variables[2], variables[3]);
				actionCounter++;
			}
			else if (variables[0].equals("CD1"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " is curing the " + variables[2] + " disease");
				board.cureDisease(Integer.parseInt(variables[1]), variables[2]);
				actionCounter++;
			}
			else if (variables[0].equals("CD2"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " is curing the " + variables[2] + " disease and throws " + variables[3] + variables[4] + variables[5] + variables[6] );
				board.cureDisease(Integer.parseInt(variables[1]), variables[2], variables[3], variables[4], variables[5], variables[6]);
				actionCounter++;
			}
			else if (variables[0].equals("AP"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " decided to pass this action");
				board.actionPass(Integer.parseInt(variables[1]));
				actionCounter++;
			}
			else if (variables[0].equals("C"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " sends the following message: " + variables[2]);
				board.chatMessage(Integer.parseInt(variables[1]), variables[2]);
			}
			else if (variables[0].equals("PA"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " plays Airlift. Moving player " + Integer.parseInt(variables[2]) + " to " + variables[3]);
				
			}
			else if (variables[0].equals("OET"))
			{
				//System.out.println("Player " + Integer.parseInt(variables[1]) + " travels to " + variables[2] + " as the Operations Expert");
				board.operationsExpertTravel(Integer.parseInt(variables[1]), variables[2], variables[3]);
				actionCounter++;
			}
			
			
			if (actionCounter >= 4)
			{
				System.out.println("\nYou reached the maximum actions for this turn..");
				break;
			}
		}
		return board;
	}
	
	// Sending desired object to specific client
	public static void sendTo(Object objToSend, ClientHandler player) throws IOException, InterruptedException {
		//System.out.println("Writing function!");
		player.dos.flush();
		player.dos.reset();
		player.dos.writeObject(objToSend);
		//System.out.println("End of writing function!");
	}
	
	// Sending desired object to all clients
	public static void sendToAll(Object objToSend) throws IOException {
		for (ClientHandler mc : Server.playerVector) {
			//System.out.println("Writing function!");
			mc.dos.flush();
			mc.dos.reset();
			mc.dos.writeObject(objToSend);
			//System.out.println("End of writing function!");
		}
	}
	
	// End of useful functions
		
} 

//ClientHandler class 
class ClientHandler implements Runnable  
{ 
	Scanner scn = new Scanner(System.in); 
	private String name; 
	final ObjectInputStream dis; 
	final ObjectOutputStream dos;
	Socket s; 
	boolean isloggedin; 
	private int playerNo;
	   
	// constructor 
	public ClientHandler(Socket s, String name, 
	                         ObjectInputStream dis, ObjectOutputStream dos, int playerNo) { 
	    this.dis = dis; 
	    this.dos = dos;  
	    this.name = name; 
	    this.s = s; 
	    this.isloggedin = true; 
	    this.playerNo = playerNo;
	} 
	
	public int getPlayerNo() {
		return this.playerNo;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public void run() {}
	
} 