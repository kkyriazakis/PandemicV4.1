package PLH512.client;

import java.io.*; 
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import PLH512.server.Board;
import PLH512.server.City;

public class Client  
{
    final static int ServerPort = 64240;
    final static String username = "myName";

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
    { 
    	int numberOfPlayers;
    	int myPlayerID;
    	String myUsername;
    	String myRole;
    	
        
        // Getting localhost ip 
        InetAddress ip = InetAddress.getByName("localhost"); 
          
        // Establish the connection 
        Socket s = new Socket(ip, ServerPort); 
        System.out.println("\nConnected to server!");
        
        // Obtaining input and out streams 
        ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream dis = new ObjectInputStream(s.getInputStream());  
        
        // Receiving the playerID from the Server
        myPlayerID = (int)dis.readObject();
        myUsername = "User_" + myPlayerID;
        System.out.println("\nHey! My username is " + myUsername);
        
        // Receiving number of players to initialize the board
        numberOfPlayers = (int)dis.readObject();
        
        // Receiving my role for this game
        myRole = (String)dis.readObject();
        System.out.println("\nHey! My role is " + myRole);
        
        // Sending the username to the Server
        dos.reset();
        dos.writeObject(myUsername);
        
        // Setting up the board
        Board[] currentBoard = {new Board(numberOfPlayers)};
        
        // Creating sendMessage thread 
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {

            	boolean timeToTalk = false;

            	//MPOREI NA GINEI WHILE  TRUE ME BREAK GIA SINTHIKI??
                while (!currentBoard[0].getGameEnded())
                {
                	timeToTalk = ((currentBoard[0].getWhoIsTalking() == myPlayerID)  && !currentBoard[0].getTalkedForThisTurn(myPlayerID));

                	try {
						TimeUnit.MILLISECONDS.sleep(15);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

                    try {
                        // Executing this part of the code once per round
                        if (timeToTalk)
                        {

                        	// Initializing variables for current round

                        	Board myBoard = copyBoard(currentBoard[0]);

							if (currentBoard[0].getGameEnded()) {
								System.exit(0);
							}

                        	String myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
                        	City myCurrentCityObj = myBoard.searchForCity(myCurrentCity);

                        	ArrayList<String> myHand = myBoard.getHandOf(myPlayerID);

                        	int[] myColorCount = {0, 0, 0, 0};

                        	for (int i = 0 ; i < 4 ; i++)
                        		myColorCount[i] =  cardsCounterOfColor(myBoard, myPlayerID, myBoard.getColors(i));

                        	ArrayList<citiesWithDistancesObj> distanceMap = new ArrayList<citiesWithDistancesObj>();
                       		distanceMap = buildDistanceMap(myBoard, myCurrentCity, distanceMap);


                        	StringBuilder myAction = new StringBuilder();
                        	String mySuggestion = "";

                        	int myActionCounter = 0;

                        	// Printing out my current hand
                        	System.out.println("\nMy current hand...");
                        	printHand(myHand);

                        	// Printing out current color count
                        	System.out.println("\nMy hand's color count...");
                        	for (int i = 0 ; i < 4 ; i++)
                        		System.out.println(myBoard.getColors(i) + " cards count: " + myColorCount[i]);

                        	// Printing out distance map from current city
                        	//System.out.println("\nDistance map from " + myCurrentCity);
                        	//printDistanceMap(distanceMap);

                        	// ADD YOUR CODE FROM HERE AND ON!!

							String finalMove = "";
							if (myBoard.getWhoIsPlaying() == myPlayerID) {
								myBoard.setActions("", myPlayerID);
								Board bestBoard = bestFourMovesLookAhead(myPlayerID, myBoard, 1);
								finalMove = bestBoard.getActions(myPlayerID);


								myBoard = copyBoard(bestBoard);
							}


                        	// UP TO HERE!! DON'T FORGET TO EDIT THE "msgToSend"

                        	// Message type
                        	// toTextShuttleFlight(0,Atlanta)+"#"+etc
                        	String msgToSend;
							assert myBoard != null;
							if (myBoard.getWhoIsPlaying() == myPlayerID)
                        		msgToSend = finalMove;

                        		//msgToSend = "AP,"+myPlayerID+"#AP,"+myPlayerID+"#AP,"+myPlayerID+"#C,"+myPlayerID+",This was my action#AP,"+myPlayerID+"#C,"+myPlayerID+",This should not be printed..";//"Action";
                            else
                        		msgToSend = "#C,"+myPlayerID+",This was my recommendation"; //"Recommendation"

                        	// NO EDIT FROM HERE AND ON (EXEPT FUNCTIONS OUTSIDE OF MAIN() OF COURSE)

                        	// Writing to Server
                        	dos.flush();
                        	dos.reset();
                        	if (!msgToSend.equals(""))
                        		msgToSend = msgToSend.substring(1); // Removing the initial delimeter
                        	dos.writeObject(msgToSend);
                        	System.out.println(myUsername + " : I've just sent my " + msgToSend);
                        	currentBoard[0].setTalkedForThisTurn(true, myPlayerID);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
					}
                }
            }
        });

        // Creating readMessage thread
        Thread readMessage = new Thread(() -> {
			while (!currentBoard[0].getGameEnded()) {
				try {

					// Reading the current board
					//System.out.println("READING!!!");
					currentBoard[0] = (Board)dis.readObject();
					//System.out.println("READ!!!");

					// Read and print Message to all clients
					String prtToScreen = currentBoard[0].getMessageToAllClients();
					if (!prtToScreen.equalsIgnoreCase(""))
						System.out.println(prtToScreen);

					// Read and print Message this client
					prtToScreen = currentBoard[0].getMessageToClient(myPlayerID);
					if (!prtToScreen.equalsIgnoreCase(""))
						System.out.println(prtToScreen);

				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
        
        // Starting the threads
        readMessage.start();
        sendMessage.start(); 
        
        // Checking if the game has ended
        while (true) 
        {
        	if (currentBoard[0].getGameEnded()) {
        		System.out.println("\nGame has finished. Closing resources.. \n");
        		//scn.close();
            	s.close();
            	System.out.println("Recources closed succesfully. Goodbye!");
            	System.exit(0);
            	break;
        	}
        }
    }

	// ============================== OUR FUNCTIONS ==============================

	public static Board SimulateNextRound(Board currentBoard){
		Board myBoard = copyBoard(currentBoard);
    	if (currentBoard.checkIfWon())
			return copyBoard(currentBoard);

		assert myBoard != null;
		myBoard.drawCards(myBoard.getWhoIsPlaying(), 2);

		if (!myBoard.getIsQuietNight())
			myBoard.infectCities(myBoard.getInfectionRate(),1);
		else
			myBoard.setIsQuietNight(false);

		myBoard.resetTalkedForThisTurn();

		if (myBoard.getWhoIsPlaying() == 3)
			myBoard.setWhoIsPlaying(0); // Back to first player
		else
			myBoard.setWhoIsPlaying(myBoard.getWhoIsPlaying() + 1); // Next player

		return myBoard;
	}

	public static Board bestFourMoves(int myPlayerID, Board currBoard, int depth){
		if (depth == 5 | currBoard.checkIfWon()){
			return copyBoard(currBoard);
		}

		Board temp = copyBoard(currBoard);
		assert temp != null;
		ArrayList<Board> children = getPossibleMoves(myPlayerID, temp);
		int children_size = children.size();

		int[] childScores = new int[children_size];
		Board[] children_new = new Board[children_size];

		for (int i=0; i<children_size; i++){
			Board child = bestFourMoves(myPlayerID, children.get(i), depth+1);
			childScores[i] = evaluatePosition(myPlayerID, child);
			children_new[i] = copyBoard(child);
		}

		int max = Integer.MIN_VALUE;
		Board best_child = null;
		for (int i=0; i<children_size; i++){
			if( childScores[i] >= max ){
				max = childScores[i];
				best_child = children_new[i];
			}
		}

		return best_child;
	}

	public static Board bestFourMovesPruned(int myPlayerID, Board currBoard, int depth){
		if (depth == 5 | currBoard.checkIfWon()){
			return copyBoard(currBoard);
		}

		Board temp = copyBoard(currBoard);
		assert temp != null;
		ArrayList<Board> children = getPossibleMoves(myPlayerID, temp);
		int children_size = children.size();

		ArrayList<Board> sorted = sortPossibleMoves(children);
		int sorted_size = children_size/(5-depth);

		int[] childScores = new int[sorted_size];
		Board[] children_new = new Board[sorted_size];

		for (int i=0; i<sorted_size; i++){
			Board child = bestFourMovesPruned(myPlayerID, sorted.get(i), depth+1);
			childScores[i] = evaluatePosition(myPlayerID, child);
			children_new[i] = copyBoard(child);
		}

		int max = Integer.MIN_VALUE;
		Board best_child = null;
		for (int i=0; i<sorted_size; i++){
			if( childScores[i] >= max ){
				max = childScores[i];
				best_child = children_new[i];
			}
		}

		return best_child;
	}

	public static Board bestFourMovesLookAhead(int myPlayerID, Board currBoard, int depth){
		if (depth == 4 | currBoard.checkIfWon()){
			return copyBoard(currBoard);
		}

		Board temp = copyBoard(currBoard);
		assert temp != null;
		ArrayList<Board> children = getPossibleMoves(myPlayerID, temp);
		int children_size = children.size();

		ArrayList<Board> sorted = sortPossibleMovesLookAhead(children);
		int sorted_size = children_size/(4-depth);

		int[] childScores = new int[sorted_size];
		Board[] children_new = new Board[sorted_size];

		for (int i=0; i<sorted_size; i++){
			Board child = bestFourMovesLookAhead(myPlayerID, sorted.get(i), depth+1);
			childScores[i] = evaluatePosition(myPlayerID, child);
			children_new[i] = copyBoard(child);
		}

		int max = Integer.MIN_VALUE;
		Board best_child = null;
		for (int i=0; i<sorted_size; i++){
			if( childScores[i] >= max ){
				max = childScores[i];
				best_child = children_new[i];
			}
		}

		return best_child;
	}

	public static int evaluatePosition(int playerID, Board CurrentBoard) {
		int score = 0;
		int cubeProtectionScale = 10;

		score += 4 - (CurrentBoard.getInfectionRate()); //Infection rate

		score += 100 - CurrentBoard.getOutbreaksCount()*10; //OutBreaks

		String role = CurrentBoard.getRoleOf(playerID); // Do stuff depending on player role
		switch (role) {
			case "Medic":
				//BETTER SCORE BECAUSE HE IS GOING TO CURE NEXT
				cubeProtectionScale = 70;
				break;
			case "Operations Expert":
				//CHECK RS NUMBER
				int RScount = CurrentBoard.getResearchStationsBuild();
				score += RScount * 70;
				break;
			case "Quarantine Specialist":
				//BETTER SCORE IF HE PROTECTS A LOT OF INFECTED CITIES
				cubeProtectionScale = 40;
				break;
			case "Scientist":
				cubeProtectionScale = 3;
				break;
		}

		String[] colors = {"Black", "Yellow", "Blue", "Red"};
		for (int i=0; i<4; i++) {
			//ADD BONUS BASED ON MY CARDS
			int cards = cardsCounterOfColor(CurrentBoard, playerID, colors[i]);
			if(cards == 2){
				score += 240;
			}
			else if(cards > 2){
				score += 450;
			}
			score += CurrentBoard.getCubesLeft(i);
		}

		//THE MORE CUBES LEFT THE BETTER
		for (int i=0; i<4; i++) {
			score += 100*CurrentBoard.getCubesLeft(i);
		}


		String myCurrentCity = CurrentBoard.getPawnsLocations(playerID);
		City myCurrentCityObj = CurrentBoard.searchForCity(myCurrentCity);
		int NeighbourCount = myCurrentCityObj.getNeighboursNumber();
		int NearbyPawns = 0;
		//Current City Cube Count
		score += cubeProtectionScale * CurrentBoard.searchForCity(myCurrentCity).getMaxCube();

		for(int n=0; n<3; n++){
			if(n!=playerID & CurrentBoard.getPawnsLocations(n).equals(myCurrentCity)){
				NearbyPawns++;
			}
		}

		//Nearby Cities Cube Count
		for (int i=0; i<NeighbourCount; i++) {
			String NeighbourName = myCurrentCityObj.getNeighbour(i);
			City NeighbourCity = CurrentBoard.searchForCity(NeighbourName);
			score += cubeProtectionScale * NeighbourCity.getMaxCube();
			for(int n=0; n<3; n++){
				if(n!=playerID & CurrentBoard.getPawnsLocations(n).equals(NeighbourName)){
					NearbyPawns++;
				}
			}
		}

		//Gets penalty if too close to other players
		score -= NearbyPawns*45;

		//Research stations built
		score += (CurrentBoard.getResearchStationsBuild()-1)*500;

		//CURED DISEASES
		for(int i=0; i<4; i++){
			if(CurrentBoard.getCured(i)){
				score += 2000;
			}
		}

		//IF WON
		if(CurrentBoard.checkIfWon())
			score = Integer.MAX_VALUE;

		return score;
	}


	public static ArrayList<Board> getPossibleMoves(int playerID, Board CurrentBoard) {
		Board tempBoard;
		String PrevActions = CurrentBoard.getActions(playerID);
		ArrayList<Board> Moves = new ArrayList<>();
		ArrayList<String> CitiesAdded = new ArrayList<>();

		//ADD ALL NEIGHBOURS ADD AS: DriveTo
		String myCurrentCity = CurrentBoard.getPawnsLocations(playerID);
		City myCurrentCityObj = CurrentBoard.searchForCity(myCurrentCity);

		for(int i=0; i<myCurrentCityObj.getNeighboursNumber(); i++) {
			String CityToGo = myCurrentCityObj.getNeighbour(i);
			CitiesAdded.add(CityToGo);

			tempBoard = copyBoard(CurrentBoard);
			assert tempBoard != null;
			tempBoard.driveTo(playerID, CityToGo);
			tempBoard.setActions(PrevActions+toTextDriveTo(playerID, CityToGo), playerID);
			Moves.add(tempBoard);
		}

		//IF Current_City HAS RS, ADD ALL AVAILABLE: ShutterFlight
		if(myCurrentCityObj.getHasReseachStation()){
			ArrayList<String> RS = CurrentBoard.getRSLocations();
			for (String CityWithRS : RS) {
				tempBoard = copyBoard(CurrentBoard);
				assert tempBoard != null;
				tempBoard.shuttleFlight(playerID, CityWithRS);
				tempBoard.setActions(PrevActions+toTextShuttleFlight(playerID, CityWithRS), playerID);
				CitiesAdded.add(CityWithRS);
				Moves.add(tempBoard);
			}
		}

		boolean currentCityAtHand = false;
		//FOR ALL CARDS(not current city or neighbours) ADD AS: DirectFlight
		ArrayList<String> Hand = CurrentBoard.getHandOf(playerID);
		for (String CityToGo : Hand) {
			if (myCurrentCity.equals(CityToGo)) {
				currentCityAtHand = true;
			}
			else if (!CitiesAdded.contains(CityToGo)) {
				tempBoard = copyBoard(CurrentBoard);
				assert tempBoard != null;
				tempBoard.directFlight(playerID, CityToGo);
				tempBoard.setActions(PrevActions+toTextDirectFlight(playerID, CityToGo), playerID);
				CitiesAdded.add(CityToGo);
				Moves.add(tempBoard);
			}
		}
		//
		//IF Operations Expert, AND IN APPROPRIATE CITY BUILD RS
		if(CurrentBoard.getRoleOf(playerID).equals("Operations Expert")){
			if(myCurrentCity.equals("Instabul") | myCurrentCity.equals("Shanghai") | myCurrentCity.equals("Sao Paulo") | myCurrentCity.equals("Chennai")){
				tempBoard = copyBoard(CurrentBoard);
				assert tempBoard != null;
				tempBoard.buildRS(playerID, myCurrentCity);
				tempBoard.setActions(PrevActions+toTextBuildRS(playerID, myCurrentCity), playerID);
				Moves.add(tempBoard);
			}
		}

		String[] BestRSlocations = {"Instabul", "Shanghai", "Sao Paulo", "Chennai"};
		//IS OP. EXPERT AND CITY WITH RS, GO EVERYWHERE
		if(CurrentBoard.getRoleOf(playerID).equals("Operations Expert") & myCurrentCityObj.getHasReseachStation()){
			//ADD BEST RS LOCATIONS THAT DONT CURRENTLY HAVE RS
			for(int i=0; i<4; i++){
				City cityToGo = CurrentBoard.searchForCity( BestRSlocations[i] );
				if(!myCurrentCity.equals(BestRSlocations[i]) & !cityToGo.getHasReseachStation()){
					tempBoard = copyBoard(CurrentBoard);
					assert tempBoard != null;
					tempBoard.operationsExpertTravel(playerID, cityToGo.getName(), Hand.get(0));
					tempBoard.setActions(PrevActions+toTextOpExpTravel(playerID, cityToGo.getName(), Hand.get(0)), playerID);
					Moves.add(tempBoard);
				}
			}
			//ADD CITIES WITH 2+ CUBES
			for(int i=0; i<CurrentBoard.getCitiesCount(); i++) {
				City cityToGo = CurrentBoard.searchForCity(i);
				if (cityToGo.getMaxCube() >= 2) {
					tempBoard = copyBoard(CurrentBoard);
					assert tempBoard != null;
					tempBoard.operationsExpertTravel(playerID, cityToGo.getName(), Hand.get(0));
					tempBoard.setActions(PrevActions+toTextOpExpTravel(playerID, cityToGo.getName(), Hand.get(0)), playerID);
					Moves.add(tempBoard);
				}
			}
		}

		//FOR CARD(==Current_City) BEST CITIES TO GO TO: charterFlight
		if (currentCityAtHand) {
			for (int i = 0; i < CurrentBoard.getCitiesCount(); i++) {
				City tempCity = CurrentBoard.searchForCity(i);
				if (tempCity.getMaxCube() >= 2 | tempCity.getHasReseachStation()) {
					tempBoard = copyBoard(CurrentBoard);
					assert tempBoard != null;
					tempBoard.charterFlight(playerID, tempCity.getName());
					tempBoard.setActions(PrevActions+toTextCharterFlight(playerID, tempCity.getName()), playerID);
					Moves.add(tempBoard);
				}
			}
		}

		//CURE A DISEASE
		tempBoard = copyBoard(CurrentBoard);
		assert tempBoard != null;
		String[] colors = {"Black", "Yellow", "Blue", "Red"};
		for (int i=0; i<4; i++) {
			if(tempBoard.cureDisease(playerID, colors[i])){
				tempBoard.setActions(PrevActions+toTextCureDisease(playerID, colors[i]), playerID);
				Moves.add(tempBoard);
			}
		}


		//TREAT A DISEASE
		tempBoard = copyBoard(CurrentBoard);
		assert tempBoard != null;
		if(tempBoard.treatDisease(playerID, myCurrentCity, myCurrentCityObj.getColour())){
			tempBoard.setActions(PrevActions+toTextTreatDisease(playerID, myCurrentCity, myCurrentCityObj.getColour()), playerID);
			Moves.add(tempBoard);
		}

		return Moves;
	}

	public static Comparator<Board> BoardScoreComp = (s1, s2) -> {
		int score1 = evaluatePosition(s1.getWhoIsPlaying(), s1);
		int score2 = evaluatePosition(s2.getWhoIsPlaying(), s2);

		return score2 - score1;
	};


	public static ArrayList<Board> sortPossibleMoves(ArrayList<Board> moves) {
		ArrayList<Board> sorted = (ArrayList<Board>) moves.clone();
		sorted.sort(BoardScoreComp);

		return sorted;
	}

	public static Comparator<Board> BoardLookAheadComp = (s1, s2) -> {
		ArrayList<Board> children_s1 = getPossibleMoves(s1.getWhoIsPlaying(), s1);
		int s1_best = Integer.MIN_VALUE;
		ArrayList<Board> children_s2 = getPossibleMoves(s2.getWhoIsPlaying(), s2);
		int s2_best = Integer.MIN_VALUE;

		int eval;
		for (Board board : children_s1) {
			eval = evaluatePosition(s1.getWhoIsPlaying(), board);
			if (eval > s1_best)
				s1_best = eval;
		}

		for (Board board : children_s2) {
			eval = evaluatePosition(s2.getWhoIsPlaying(), board);
			if (eval > s2_best)
				s2_best = eval;
		}

		return s2_best - s1_best;
	};


	public static ArrayList<Board> sortPossibleMovesLookAhead(ArrayList<Board> moves) {
		ArrayList<Board> sorted = (ArrayList<Board>) moves.clone();
		sorted.sort(BoardLookAheadComp);

		return sorted;
	}

	// ============================== OUR FUNCTIONS UP TO HERE ==============================


	// --> Useful functions <--
    
    public static Board copyBoard (Board boardToCopy)
    {
    	Board copyOfBoard;
    	
    	try {
    	     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	     ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
    	     outputStrm.writeObject(boardToCopy);
    	     ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    	     ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
    	     copyOfBoard = (Board)objInputStream.readObject();
    	     return copyOfBoard;
    	   }
    	   catch (Exception e) {
    	     e.printStackTrace();
    	     return null;
    	   }
    }
    
    public static String getDirectionToMove (String startingCity, String goalCity, ArrayList<citiesWithDistancesObj> distanceMap, Board myBoard) {
    	City startingCityObj = myBoard.searchForCity(startingCity);
    	
    	int minDistance = distanceFrom(goalCity, distanceMap);
    	int testDistance;
    	
    	String directionToDrive = null;
    	String testCity = null;
    	
    	for (int i = 0 ; i < startingCityObj.getNeighboursNumber() ; i++)
    	{
    		ArrayList<citiesWithDistancesObj> testDistanceMap = new ArrayList<citiesWithDistancesObj>();
    		testDistanceMap.clear();
    		
    		testCity = startingCityObj.getNeighbour(i);
    		testDistanceMap = buildDistanceMap(myBoard, testCity, testDistanceMap);
    		testDistance = distanceFrom(goalCity, testDistanceMap);
    		
    		if (testDistance < minDistance)
    		{
    			minDistance = testDistance;
    			directionToDrive = testCity;
    		}
    	}
    	return directionToDrive;
    }
    
    
    public static String getMostInfectedInRadius(int radius, ArrayList<citiesWithDistancesObj> distanceMap, Board myBoard)
    {
    	int maxCubes = -1;
    	String mostInfected = null;

		for (PLH512.client.citiesWithDistancesObj citiesWithDistancesObj : distanceMap) {
			if (citiesWithDistancesObj.getDistance() <= radius) {
				City cityToCheck = myBoard.searchForCity(citiesWithDistancesObj.getName());

				if (cityToCheck.getMaxCube() > maxCubes) {
					mostInfected = cityToCheck.getName();
					maxCubes = cityToCheck.getMaxCube();
				}
			}
		}
    	
    	return mostInfected;
    }
    
    // Count how many card of the color X player X has
    public static int cardsCounterOfColor(Board board, int  playerID, String color)
    {
    	int cardsCounter = 0;
    	
    	for (int i = 0 ; i < board.getHandOf(playerID).size() ; i++)
    		if (board.searchForCity(board.getHandOf(playerID).get(i)).getColour().equals(color))
    			cardsCounter++;
    	
    	return cardsCounter;
    }
    
    public static void printHand(ArrayList<String> handToPrint) {
		for (String s : handToPrint) System.out.println(s);
    }
    
    public static boolean alredyInDistanceMap(ArrayList<citiesWithDistancesObj> currentMap, String cityName) {
		for (PLH512.client.citiesWithDistancesObj citiesWithDistancesObj : currentMap)
			if (citiesWithDistancesObj.getName().equals(cityName))
				return true;
    	
    	return false;
    }
    
    public static boolean isInDistanceMap (ArrayList<citiesWithDistancesObj> currentMap, String cityName) {
		for (PLH512.client.citiesWithDistancesObj citiesWithDistancesObj : currentMap) {
			if (citiesWithDistancesObj.getName().equals(cityName))
				return true;
		}
    	return false;
    }
    
    public static void printDistanceMap(ArrayList<citiesWithDistancesObj> currentMap) {
		for (PLH512.client.citiesWithDistancesObj citiesWithDistancesObj : currentMap)
			System.out.println("Distance from " + citiesWithDistancesObj.getName() + ": " + citiesWithDistancesObj.getDistance());
    }
    
    public static int distanceFrom(String cityToFind, ArrayList<citiesWithDistancesObj> currentDistanceMap) {
    	int result = -1;

		for (PLH512.client.citiesWithDistancesObj citiesWithDistancesObj : currentDistanceMap)
			if (citiesWithDistancesObj.getName().equals(cityToFind))
				result = citiesWithDistancesObj.getDistance();
    	
    	return result;
    }
    
    public static int numberOfCitiesWithDistance(int distance, ArrayList<citiesWithDistancesObj> currentDistanceMap) {
    	int count = 0;
		for (PLH512.client.citiesWithDistancesObj citiesWithDistancesObj : currentDistanceMap)
			if (citiesWithDistancesObj.getDistance() == distance)
				count++;
    	
    	return count;
    }
    
    public static ArrayList<citiesWithDistancesObj> buildDistanceMap(Board myBoard, String currentCityName, ArrayList<citiesWithDistancesObj> currentMap) {
    	currentMap.clear();
    	currentMap.add(new citiesWithDistancesObj(currentCityName, myBoard.searchForCity(currentCityName), 0));

    	for (int n = 0 ; n < 15 ; n++) {
			for (int i = 0; i < currentMap.size(); i++) {
				if (currentMap.get(i).getDistance() == (n - 1)) {
					for (int j = 0; j < currentMap.get(i).getCityObj().getNeighboursNumber(); j++) {
						String nameOfNeighbor = currentMap.get(i).getCityObj().getNeighbour(j);

						if (!(alredyInDistanceMap(currentMap, nameOfNeighbor)))
							currentMap.add(new citiesWithDistancesObj(nameOfNeighbor, myBoard.searchForCity(nameOfNeighbor), n));
					}
				}
			}
		}
    	return currentMap;
    }
    
    
    // --> Actions <--
    
    
    // --> Coding functions <--
    
    public static String toTextDriveTo(int playerID, String destination) { return "#DT,"+playerID+","+destination; }
    	
    public static String toTextDirectFlight(int playerID, String destination) { return "#DF,"+playerID+","+destination; }
    
    public static String toTextCharterFlight(int playerID, String destination) { return "#CF,"+playerID+","+destination; }
    
    public static String toTextShuttleFlight(int playerID, String destination) { return "#SF,"+playerID+","+destination; }
    
    public static String toTextBuildRS(int playerID, String destination) { return "#BRS,"+playerID+","+destination; }
    
    public static String toTextRemoveRS(int playerID, String destination) { return "#RRS,"+playerID+","+destination; }
    
    public static String toTextTreatDisease(int playerID, String destination, String color) { return "#TD,"+playerID+","+destination+","+color; }
    
    public static String toTextCureDisease(int playerID, String color) { return "#CD1,"+playerID+","+color; }

	public static String toTextCureDisease(int playerID, String color, String card1, String card2, String card3, String card4){
		return "#CD2,"+playerID+","+color+","+card1+","+card2+","+card3+","+card4;
	}
    
    public static String toTextShareKnowledge(boolean giveOrTake, String cardToSwap, int myID, int playerIDToSwap) {
    	return "#SK,"+giveOrTake+","+cardToSwap+","+myID+","+playerIDToSwap;
    }
    
    public static String toTextActionPass(int playerID) {
    	return "#AP,"+playerID;
    }
    
    public static String toTextChatMessage(int playerID, String messageToSend) {
    	return "#C,"+playerID+","+messageToSend;
    }
    
    public static String toTextPlayGG(int playerID, String cityToBuild) {
    	return "#PGG,"+playerID+","+cityToBuild;
    }
    
    public static String toTextPlayQN(int playerID) {
    	return "#PQN,"+playerID;
    }

    public static String toTextPlayA(int playerID, int playerToMove, String cityToMoveTo) {
    	return "#PA,"+playerID+","+playerToMove+","+cityToMoveTo;
    }
    public static String toTextPlayF(int playerID) {
    	return "#PF,"+playerID;
    }
    public static String toTextPlayRP(int playerID, String cityCardToRemove) {
    	return "#PRP,"+playerID+","+cityCardToRemove;
    }

	public static String toTextOpExpTravel(int playerID, String destination, String colorToThrow){
		return "#OET,"+playerID+","+destination+","+colorToThrow;
	}

} 