package PLH512.client;

import java.io.*; 
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
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
                while (currentBoard[0].getGameEnded() == false)
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

                        	Board myBoard = currentBoard[0];

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

							ArrayList<Board> CHILDREN = getPossibleMoves(myPlayerID, myBoard);
							for (int i=0; i<CHILDREN.size(); i++){
								int val = evaluatePosition(myPlayerID, CHILDREN.get(i));
								System.out.println(i+ ". At City(" +CHILDREN.get(i).getPawnsLocations(myPlayerID)+ ") Evaluation("+ val +")");
							}


                        	boolean tryToCure = false;
                        	String colorToCure = null;

                        	boolean tryToTreatHere = false;
                        	String colorToTreat;

                        	boolean tryToTreatClose = false;
                        	String destinationClose = null;

                        	boolean tryToTreatMedium = false;
                        	String destinationMedium = null;

                        	if (myColorCount[0] > 4 || myColorCount[1] > 4 || myColorCount[2] > 4 || myColorCount[3] > 4) {
								tryToCure = true;

                        		if (myColorCount[0] > 4)
                        			colorToCure = "Black";
                        		else if (myColorCount[1] > 4)
                        			colorToCure = "Yellow";
                        		else if (myColorCount[2] > 4)
                        			colorToCure = "Blue";
                        		else colorToCure = "Red";
                        	}

                        	if (tryToCure)
                        	{
                        		System.out.println("I want to try and cure the " + colorToCure + " disease!");
                        		myAction.append(toTextCureDisease(myPlayerID, colorToCure));
                        		myBoard.cureDisease(myPlayerID, colorToCure);
                        		myActionCounter++;

                        	}

                        	if (myCurrentCityObj.getBlackCubes() != 0 || myCurrentCityObj.getYellowCubes() != 0  || myCurrentCityObj.getBlueCubes() != 0  || myCurrentCityObj.getRedCubes() != 0) {
                        		if (myActionCounter < 4)
                        			tryToTreatHere = true;
                        	}

                        	if (tryToTreatHere) {
                        		while (myCurrentCityObj.getMaxCube() != 0 && myActionCounter < 4) {
                        			colorToTreat = myCurrentCityObj.getMaxCubeColor();

                    				System.out.println("I want to try and treat one " + colorToTreat + " cube from " + myCurrentCity + "!");

                    				myAction.append(toTextTreatDisease(myPlayerID, myCurrentCity, colorToTreat));
                            		myActionCounter++;

                            		myBoard.treatDisease(myPlayerID, myCurrentCity, colorToTreat);
                        		}
                        	}

                        	if (myActionCounter < 4 )
                        	{
                        		destinationClose = getMostInfectedInRadius(1, distanceMap, myBoard);

                        		if(!destinationClose.equals(myCurrentCity))
                        			tryToTreatClose = true;
                    		}

                        	if (tryToTreatClose)
                        	{
                        		System.out.println("Hhhmmmmmm I could go and try to treat " + destinationClose);

                        		myAction.append(toTextDriveTo(myPlayerID, destinationClose));
                        		myActionCounter++;

                        		myBoard.driveTo(myPlayerID, destinationClose);

                        		myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
                            	myCurrentCityObj = myBoard.searchForCity(myCurrentCity);

                        		while (myCurrentCityObj.getMaxCube() != 0 && myActionCounter < 4) {
                        			colorToTreat = myCurrentCityObj.getMaxCubeColor();

                    				System.out.println("I want to try and treat one " + colorToTreat + " cube from " + myCurrentCity + "!");

                    				myAction.append(toTextTreatDisease(myPlayerID, myCurrentCity, colorToTreat));
                            		myActionCounter++;

                            		myBoard.treatDisease(myPlayerID, myCurrentCity, colorToTreat);
                        		}
                        	}


                        	if (myActionCounter < 4 )
                        	{
                        		destinationMedium = getMostInfectedInRadius(2, distanceMap, myBoard);

                        		if(!destinationMedium.equals(myCurrentCity))
                        			tryToTreatMedium = true;
                    		}

                        	if (tryToTreatMedium)
                        	{
                        		System.out.println("Hhhmmmmmm I could go and try to treat " + destinationMedium);

                        		String driveFirstTo = getDirectionToMove(myCurrentCity, destinationMedium, distanceMap, myBoard);

                        		myAction.append(toTextDriveTo(myPlayerID, driveFirstTo));
                        		myActionCounter++;
                        		myAction.append(toTextDriveTo(myPlayerID, destinationMedium));
                        		myActionCounter++;

                        		myBoard.driveTo(myPlayerID, driveFirstTo);

                        		myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
                            	myCurrentCityObj = myBoard.searchForCity(myCurrentCity);

                        		myBoard.driveTo(myPlayerID, destinationMedium);

                        		myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
                            	myCurrentCityObj = myBoard.searchForCity(myCurrentCity);

                        		while (myCurrentCityObj.getMaxCube() != 0 && myActionCounter < 4)
                        		{
                        			colorToTreat = myCurrentCityObj.getMaxCubeColor();

                    				System.out.println("I want to try and treat one " + colorToTreat + " cube from " + myCurrentCity + "!");

                    				myAction.append(toTextTreatDisease(myPlayerID, myCurrentCity, colorToTreat));
                            		myActionCounter++;

                            		myBoard.treatDisease(myPlayerID, myCurrentCity, colorToTreat);
                        		}
                        	}

                        	Random rand = new Random();

                        	while (myActionCounter < 4)
                        	{
                        		int upperBound;
                        		int randomNumber;
                        		String randomCityToGo;

                        		upperBound = myCurrentCityObj.getNeighboursNumber();
                        		randomNumber = rand.nextInt(upperBound);
                        		randomCityToGo = myCurrentCityObj.getNeighbour(randomNumber);

                        		System.out.println("Moving randomly to " + randomCityToGo);

                        		myAction.append(toTextDriveTo(myPlayerID, randomCityToGo));
                        		myActionCounter++;

                        		myBoard.driveTo(myPlayerID, randomCityToGo);

                        		myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
                            	myCurrentCityObj = myBoard.searchForCity(myCurrentCity);
                    		}


                        	// UP TO HERE!! DON'T FORGET TO EDIT THE "msgToSend"

                        	// Message type
                        	// toTextShuttleFlight(0,Atlanta)+"#"+etc
                        	String msgToSend;
                        	if (myBoard.getWhoIsPlaying() == myPlayerID)
                        		msgToSend = myAction.toString();

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
        Thread readMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() {
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
	public static int evaluatePosition(int playerID, Board CurrentBoard) {
		int score = 0;
		int cubeProtectionScale = 5;

		//TODO: ADD COUNT OF CARDS IN HAND

		score += 4 - (CurrentBoard.getInfectionRate()); //Infection rate

		score += 70 - CurrentBoard.getOutbreaksCount()*10; //OutBreaks

		String role = CurrentBoard.getRoleOf(playerID); // Do stuff depending on player role
		switch (role) {
			case "Medic":
				//TODO: FOR A CURED DISEASE, CHECK NEARBY CUBES YOU CAN REMOVE
				score += 1;
				break;
			case "Operations Expert":
				//CHECK RS NUMBER
				int RScount = CurrentBoard.getResearchStationsBuild();
				score += RScount * 70;

				break;
			case "Quarantine Specialist":
				//BETTER SCORE IF HE PROTECTS A LOT OF INFECTED CITIES
				cubeProtectionScale = 15;
				break;
			case "Scientist":
				score += 4;
				break;
		}

		String myCurrentCity = CurrentBoard.getPawnsLocations(playerID);
		City myCurrentCityObj = CurrentBoard.searchForCity(myCurrentCity);
		int NeighbourCount = myCurrentCityObj.getNeighboursNumber();
		//Current City Cube Count
		score += cubeProtectionScale * CurrentBoard.searchForCity(myCurrentCity).getMaxCube();

		//Nearby Cities Cube Count
		for (int i=0; i<NeighbourCount; i++) {
			String NeighbourName = myCurrentCityObj.getNeighbour(i);
			City NeighbourCity = CurrentBoard.searchForCity(NeighbourName);

			score += cubeProtectionScale * NeighbourCity.getMaxCube();
		}

		//Research stations built
		score += CurrentBoard.getResearchStationsBuild()*10;

		//CURED DISEASES
		for(int i=0; i<4; i++){
			if(CurrentBoard.getCured(i)){
				score += 50;
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
					//tempBoard.operationsExpertTravel(playerID, cityToGo.getName(), Hand.get(0));
					//tempBoard.setActions(PrevActions+toTextoperationsExpertTravel(playerID, cityToGo.getName(), Hand.get(0)), playerID);
					//Moves.add(tempBoard);
				}
			}
			//ADD CITIES WITH 3 CUBES
			for(int i=0; i<CurrentBoard.getCitiesCount(); i++) {
				City cityToGo = CurrentBoard.searchForCity(i);
				if (cityToGo.getMaxCube() == 3) {
					tempBoard = copyBoard(CurrentBoard);
					assert tempBoard != null;
					//tempBoard.operationsExpertTravel(playerID, cityToGo.getName(), Hand.get(0));
					//tempBoard.setActions(PrevActions+toTextoperationsExpertTravel(playerID, cityToGo.getName(), Hand.get(0)), playerID);
					//Moves.add(tempBoard);
				}
			}
		}

		//FOR CARD(==Current_City) BEST CITIES TO GO TO: charterFlight
		if (currentCityAtHand) {
			for (int i = 0; i < CurrentBoard.getCitiesCount(); i++) {
				City tempCity = CurrentBoard.searchForCity(i);
				if (tempCity.getMaxCube() == 3 | tempCity.getHasReseachStation()) {
					tempBoard = copyBoard(CurrentBoard);
					assert tempBoard != null;
					tempBoard.charterFlight(playerID, tempCity.getName());
					tempBoard.setActions(PrevActions+toTextCharterFlight(playerID, myCurrentCity), playerID);
					Moves.add(tempBoard);
				}
			}
		}

		//CURE A DISEASE
		tempBoard = copyBoard(CurrentBoard);
		assert tempBoard != null;
		if(tempBoard.cureDisease(playerID, myCurrentCityObj.getColour())){
			tempBoard.setActions(PrevActions+toTextCureDisease(playerID, myCurrentCityObj.getColour()), playerID);
			Moves.add(tempBoard);
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
    
    public static String toTextCureDisease(int playerID, String color, String card1, String card2, String card3, String card4, String card5) {
    	return "#CD2,"+playerID+","+color+","+card1+","+card2+","+card3+","+card4+","+card5;
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

} 