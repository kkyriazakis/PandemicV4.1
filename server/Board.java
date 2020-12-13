package PLH512.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

@SuppressWarnings("serial")
public class Board implements Serializable{

	// --> Variables declaration <--
	
	private String[] colors = {"Black", "Yellow", "Blue", "Red"};
	
	// Game setup variables from coming from Server
	
	private int numberOfPlayers;
	
	// Game setup variables that are defined here
	
	private int numberOfEpidemicCards = 4; // From 4 to 6
	private int numberOfEventCards = 0; // From 0 to 5
	private int cardsNeededForCure = 4;
	private int RSLimit = 6;
	private int[] infectionRate = {2,2,2,3,3,4,4};
	private int outbreaksLimit = 8;
	private int initialCubesLimit = 24; // per color
	private String[] roles = {"Medic", "Operations Expert", "Quarantine Specialist", "Scientist"};
	private boolean isLimitSeven = false;
	
	// Other variables
	
	private String[] usernames  = {"","","",""};
	private String[] pawnsLocations  = {"","","",""};
	
	private ArrayList<String> hand0 = new ArrayList<String>();
	private ArrayList<String> hand1 = new ArrayList<String>();
	private ArrayList<String> hand2 = new ArrayList<String>();
	private ArrayList<String> hand3 = new ArrayList<String>();
	
	private ArrayList<String> playersDeck = new ArrayList<String>();
	private ArrayList<String> infectedDeck = new ArrayList<String>();
	private ArrayList<String> discardedPile = new ArrayList<String>();
	
	private String[] actions  = {"","","",""};
	private boolean[] talkedForThisTurn = {true, true, true, true};
	
	private int round;
	private int whoIsPlaying;
	private int whoIsTalking;
	private boolean gameEnded;
	
	private String messageToAllClients = "";
	private String[] messageToClient = {"","","",""};
	
	private int[] cubesLeft = {initialCubesLimit, initialCubesLimit, initialCubesLimit, initialCubesLimit};
	private boolean[] cured = {false, false, false, false};
	private boolean[] erradicated = {false, false, false, false};
	
	private int RSCount = 0;
	private ArrayList<String> RSLocations = new ArrayList<String>();
	
	private int outbreaksCount;
	private ArrayList<String> canNotOutbreak = new ArrayList<String>();
	
	private boolean isInitianInfection = false;
	private ArrayList<String> underQuarantineSpecialistProtection = new ArrayList<String>();
	
	private ArrayList<String> underMedicProtection = new ArrayList<String>();
	
	private int currentInfectionPointer;
	
	private Vector<City> cityList = new Vector<>();
	private ArrayList<String> cityNamesList = new ArrayList<String>();
	
	private boolean isQuietNight = false;
	
	// --> Constructors Declaration <--
	
	// Simple constructor with just the players number as parameter
	public Board(int numberOfPlayers) {
		this.numberOfPlayers = numberOfPlayers;
		this.gameEnded = false;
	}
	
	// --> Functions Declaration <--
	
	// Initializing functions 
	
	// Just shuffling the roles for players to get randomly
	public void initializeRoles()
	{
		Collections.shuffle(Arrays.asList(this.roles));
	}
	
	// Building the city map with neighbors
	public void initializeCities()
	{
		System.out.println("Initializing cities...");
		
		//Black Cities
		cityList.add(new City("Algiers", "Black", 4, "Cairo", "Instabul", "Paris", "Madrid", "", ""));
		cityList.add(new City("Baghdad", "Black", 5, "Karachi", "Tehran", "Instabul", "Cairo", "Riyadh", ""));
		cityList.add(new City("Cairo", "Black", 5, "Riyadh", "Baghdad", "Instabul", "Algiers", "Khartoum", ""));
		cityList.add(new City("Chennai", "Black", 5, "Kolkata", "Delhi", "Mumbai", "Jakarta", "Bangkok", ""));
		cityList.add(new City("Delhi", "Black", 5, "Kolkata", "Tehran", "Karachi", "Mumbai", "Chennai", ""));
		cityList.add(new City("Instabul", "Black", 6, "Baghdad", "Moscow", "St. Petersburg", "Milan", "Algiers", "Cairo"));
		cityList.add(new City("Karachi", "Black", 5, "Delhi", "Tehran", "Baghdad", "Riyadh", "Mumbai", ""));		
		cityList.add(new City("Kolkata", "Black", 4, "Chennai", "Delhi", "Hong Kong", "Bangkok", "", ""));		
		cityList.add(new City("Moscow", "Black", 3, "Tehran", "St. Petersburg", "Instabul", "", "", ""));		
		cityList.add(new City("Mumbai", "Black", 3, "Chennai", "Delhi", "Karachi", "", "", ""));		
		cityList.add(new City("Riyadh", "Black", 3, "Karachi", "Baghdad", "Cairo", "", "", ""));		
		cityList.add(new City("Tehran", "Black", 4, "Delhi", "Moscow", "Baghdad", "Karachi", "", ""));
		
		//Yellow Cities
		cityList.add(new City("Bogota", "Yellow", 5, "Miami", "Mexico City", "Lima", "Buenos Aires", "Sao Paulo", ""));		
		cityList.add(new City("Buenos Aires", "Yellow", 2, "Bogota", "Sao Paulo", "", "", "", ""));
		cityList.add(new City("Johannesburg", "Yellow", 2, "Kinshasa", "Khartoum", "", "", "", ""));
		cityList.add(new City("Khartoum", "Yellow", 4, "Cairo", "Lagos", "Kinshasa", "Johannesburg", "", ""));
		cityList.add(new City("Kinshasa", "Yellow", 3, "Lagos", "Khartoum", "Johannesburg", "", "", ""));
		cityList.add(new City("Lagos", "Yellow", 3, "Khartoum", "Kinshasa", "Sao Paulo", "", "", ""));
		cityList.add(new City("Lima", "Yellow", 3, "Mexico City", "Bogota", "Santiago", "", "", ""));
		cityList.add(new City("Los Angeles", "Yellow", 4, "Mexico City", "Chicago", "San Fransisco", "Sydney", "", ""));
		cityList.add(new City("Mexico City", "Yellow", 5, "Los Angeles", "Chicago", "Miami", "Bogota", "Lima", ""));
		cityList.add(new City("Miami", "Yellow", 4, "Bogota", "Mexico City", "Atlanta", "Washington", "", ""));
		cityList.add(new City("Santiago", "Yellow", 1, "Lima", "", "", "", "", ""));
		cityList.add(new City("Sao Paulo", "Yellow", 4, "Buenos Aires", "Bogota", "Madrid", "Lagos", "", ""));
		
		// Blue Cities
		cityList.add(new City("Atlanta", "Blue", 3, "Miami", "Washington", "Chicago", "", "", ""));
		cityList.add(new City("Chicago", "Blue", 5, "Montreal", "Atlanta", "Mexico City", "Los Angeles", "San Fransisco", ""));
		cityList.add(new City("Essen", "Blue", 4, "St. Petersburg", "Milan", "Paris", "London", "", ""));
		cityList.add(new City("London", "Blue", 4, "Essen", "Paris", "Madrid", "New York", "", ""));
		cityList.add(new City("Madrid", "Blue", 5, "Sao Paulo", "Algiers", "Paris", "London", "New York", ""));		
		cityList.add(new City("Milan", "Blue", 3, "Instabul", "Essen", "Paris", "", "", ""));
		cityList.add(new City("Montreal", "Blue", 3, "New York", "Washington", "Chicago", "", "", ""));
		cityList.add(new City("New York", "Blue", 4, "London", "Madrid", "Washington", "Montreal", "", ""));
		cityList.add(new City("Paris", "Blue", 5, "Algiers", "Milan", "Essen", "London", "Madrid", ""));
		cityList.add(new City("San Fransisco", "Blue", 4, "Chicago", "Los Angeles", "Tokyo", "Manila", "", ""));
		cityList.add(new City("St. Petersburg", "Blue", 3, "Essen", "Moscow", "Instabul", "", "", ""));
		cityList.add(new City("Washington", "Blue", 4, "New York", "Montreal", "Atlanta", "Miami", "", ""));
		
		// Red Cities
		cityList.add(new City("Bangkok", "Red", 5, "Kolkata", "Chennai", "Hong Kong", "Ho Chi Minh City", "Jakarta", ""));
		cityList.add(new City("Beijing", "Red", 2, "Seoul", "Shanghai", "", "", "", ""));
		cityList.add(new City("Ho Chi Minh City", "Red", 4, "Jakarta", "Bangkok", "Hong Kong", "Manila", "", ""));
		cityList.add(new City("Hong Kong", "Red", 6, "Kolkata", "Bangkok", "Ho Chi Minh City", "Manila", "Taipei", "Shanghai"));
		cityList.add(new City("Jakarta", "Red", 4, "Chennai", "Bangkok", "Ho Chi Minh City", "Sydney", "", ""));
		cityList.add(new City("Manila", "Red", 5, "Sydney", "Ho Chi Minh City", "Hong Kong", "Taipei", "San Fransisco", ""));
		cityList.add(new City("Osaka", "Red", 2, "Tokyo", "Taipei", "", "", "", ""));
		cityList.add(new City("Seoul", "Red", 3, "Beijing", "Shanghai", "Tokyo", "", "", ""));
		cityList.add(new City("Shanghai", "Red", 5, "Beijing", "Seoul", "Tokyo", "Taipei", "Hong Kong", ""));
		cityList.add(new City("Sydney", "Red", 3, "Jakarta", "Manila", "Los Angeles", "", "", ""));
		cityList.add(new City("Taipei", "Red", 4, "Osaka", "Shanghai", "Hong Kong", "Manila", "", ""));
		cityList.add(new City("Tokyo", "Red", 4, "San Fransisco", "Seoul", "Shanghai", "Osaka", "", ""));
		
		for (int i = 0 ; i < cityList.size() ; i++)
			cityNamesList.add(cityList.get(i).getName());
	}
	
	public int getCitiesCount()
	{
		return cityList.size();	
	}
	
	// The initial infection. 3 cities with 3 cubes, 3 with 2 and 3 with 1
	public void initialInfection()
	{
		isInitianInfection = true;
		infectCities(3,3);
		infectCities(3,2);
		infectCities(3,1);
		isInitianInfection = false;
	}
	
	// Building up and shuffling the Infection Deck
	public void initializeInfectedDeck()
	{
		System.out.println("Initializing infected deck...");
		for(City city:cityList) 
            infectedDeck.add(city.getName());
		Collections.shuffle(infectedDeck);
	}
	
	// Building up and shuffling the Players Deck without special and epidemic cards
	public void initializePlayersDeck()
	{
		System.out.println("Initializing players deck...");
		for(City city:cityList) 
            playersDeck.add(city.getName());
		Collections.shuffle(playersDeck);
	}
	
	// Adding Epidemic Cards to Players Deck and shuffling
	public void addEpidemicCards()
	{
		System.out.println("Adding " + numberOfEpidemicCards + " Epidemic cards to the player Deck..\n");
		for(int i=0 ; i < numberOfEpidemicCards ; i++)
			playersDeck.add("Epidemic");
		Collections.shuffle(playersDeck);
	}
	

	// Giving out the cards to players for first time (before we add epidemic cards)
	public void initializePlayersHands()
	{
		System.out.println("Giving out first cards to players...\n");
		
		int cardsToTake;
		
		if (numberOfPlayers == 2)
			cardsToTake = 4;
		else if (numberOfPlayers == 3)
			cardsToTake = 3;
		else
			cardsToTake = 2;
		
		for(int i=0 ; i < numberOfPlayers ; i++)
		{
			for(int j=0 ; j < cardsToTake ; j++)
			{
				System.out.println(usernames[i] + " just drawn the " + playersDeck.get(0).toString() + " card");
				//System.out.println("Giving " + playersDeck.get(0).toString() + " to " + usernames[i] + " and removing from player deck..");
				getHandOf(i).add(playersDeck.get(0).toString());
				playersDeck.remove(0);
			}
		}
		System.out.println();
	}
	
	// Setting every pawn to Atlanta
	public void initializePawnsPositions()
	{
		System.out.println("Getting everyone to Atlanta...");
		
		for (int i = 0 ; i < numberOfPlayers ; i++)
			setPawnsLocations(i, "Atlanta");
	}
	
	// Initializing some more game variables that we are gonna need
	public void initializeGameVariables()
	{
		setRound(1);
		setWhoIsPlaying(0);
		setWhoIsTalking(5);
		setGameEnded(false);
		setInfectionPointer(0);
	}
	
	// Printing functions
	
	// Printing all cities with current cubes in them
	public void printCitiesAndCubes()
	{
		int count = 0;
		
		for(City city : cityList)
		{	
			if (count == 0)
				System.out.println(" ----- Black Cities  ----- ");
			else if (count == 12)
				System.out.println("\n ----- Yellow Cities  ----- ");
			else if (count == 24)
				System.out.println("\n ------ Blue Cities  ------ ");
			else if (count == 36)
				System.out.println("\n ------ Red Cities  ------ ");
			
			count++;
			
			System.out.println(city.getName() + "[" + city.getBlackCubes() + ", " + city.getYellowCubes() + ", " + city.getBlueCubes() + ", " + city.getRedCubes() + "]");
		}
			System.out.println();
	}
	
	// Printing remaining cubes of each disease and cured / eradicated status
	public void printWhoIsPlayngWithRole()
	{
		System.out.println(usernames[whoIsPlaying] + " is playing now as the " + roles[whoIsPlaying]);
		System.out.println();
	}
	
	// Printing remaining cubes of each disease and cured / eradicated status
	public void printRemainingCubesAndDiseaseStatus()
	{
		System.out.println(" ----- Aggregate Info After Set of Actions ----- ");
		
		int[] cubesLeft = {-1,-1,-1,-1};
		String[] status = {"Active", "Active", "Active", "Active"};
		int epidemicCardsLeft = 0;
		
		for (int i = 0 ; i < 4 ; i++)
		{
			if (erradicated[i])
				status[i] = "Erradicated";
			else if (cured[i])
				status[i] = "Cured";
			
			cubesLeft[i] = getCubesLeft(i);
		}
		
		for (int i = 0 ; i < playersDeck.size() ; i++)
			if (playersDeck.get(i).equals("Epidemic"))
				epidemicCardsLeft++;
		
		System.out.println("Cubes Left : " + Arrays.toString(cubesLeft));
		System.out.println("Cards To Draw Left : " + playersDeck.size());
		System.out.println("Disease Status : " + Arrays.toString(status));
		System.out.println("Outbreak Status: " + outbreaksCount + " outbreaks have already occured (max is " + outbreaksLimit + " outbreaks)");
		System.out.println("Epidemic Status : " + epidemicCardsLeft + " Epidemic cards left out of " +numberOfEpidemicCards);
		
		System.out.println();
	}
	
	// Printing out the array of the roles of the players
	public void printOutRoles()
	{
		for(int i = 0 ; i < this.numberOfPlayers ; i++)
			System.out.println(this.usernames[i] + " --> " + this.roles[i]);
	}
	
	// Prints out the hand of the desired player
	public void printHandOf(int playerID)
	{
		//System.out.println("Printing out " + usernames[playerID] + "'s hand...");
		for(String card:getHandOf(playerID)) 
            System.out.println(card);
		System.out.println("");
	}
		
	// Prints the player Deck
	public void printPlayersDeck()
	{
		for(String city:playersDeck) 
            System.out.println(city);
		System.out.println("");
	}
	
	// Searching functions
	
	// Searches for the players and returns the array list (aka hand) of him/her
	public ArrayList<String> getHandOf(int playerID)
	{
		if (playerID == 0)
			return hand0;
		else if (playerID == 1)
			return hand1;
		else if (playerID == 2)
			return hand2;
		else if (playerID == 3)
			return hand3;
		else
		{
			System.out.println("Please import a valid player ID number...");
			return null;
		}
	}
	
	// Searches for desired city and returns the city object
	public City searchForCity(String cityName)
	{
		City result = null;
		
		for (City tmpCity : cityList)
		{
			if (tmpCity.getName().equals(cityName))
				result = tmpCity;
		}
		
		return result;
	}
	
	public City searchForCity(int cityID)
	{
		return cityList.get(cityID);
	}
	
	public boolean isCity(String cardName)
	{
		return cityNamesList.contains(cardName);
	}
	
	// Searches for desired city and returns the color of the city
	public String colorOf(String cityName)
	{
		String result = null;
		
		for (City tmpCity : cityList)
		{
			if (tmpCity.getName().equals(cityName))
				result = tmpCity.getColour();
		}
		
		return result;
	}
	
		
	// Adding x cubes of x color to x city and checking for outbreak
	public void addCubes(String cityToAddCubes, int numberOfCubes, String cubeColor)
	{
		updateQuarantineSpecialistProtectionList();
		updateMedicProtectionList(cubeColor);
		
		//System.out.println("Cured list: " + cured[0] + cured[1] + cured[2] + cured[3]);
		//System.out.println("Eradicated list: " + erradicated[0] + erradicated[1] + erradicated[2] + erradicated[3]);
		
		if (underQuarantineSpecialistProtection.contains(cityToAddCubes) && !isInitianInfection) {
			System.out.println("Cannot infect " + cityToAddCubes + " cause it's under the Quarantine Specialist's protection");
			return;
		}
		
		if (underMedicProtection.contains(cityToAddCubes) && !isInitianInfection) {
			System.out.println("Cannot infect " + cityToAddCubes + " cause it's under the Medic's protection");
			return;
		}
		
		if (checkIfEradicated(cubeColor)) {
			System.out.println("Cannot infect " + cityToAddCubes + " cause the disease has been eradicated");
			return;
		}
		
		System.out.println("Adding " + numberOfCubes + " " + cubeColor + " cube(s) to " + cityToAddCubes);
		
		int indexOfCity;
		boolean outbreak;
		String outbreakColor;
		int cubesAlreadyThere;
		
		indexOfCity = cityList.indexOf(searchForCity(cityToAddCubes));
		outbreak = false;
		outbreakColor = "";
		
		if (cubeColor == "Black" && getCubesLeft(0) >= numberOfCubes)
		{
			cubesAlreadyThere = cityList.get(indexOfCity).getBlackCubes();
			cityList.get(indexOfCity).setBlackCubes(cityList.get(indexOfCity).getBlackCubes() + numberOfCubes);
			outbreak = cityList.get(indexOfCity).checkOutbreak();
			if (outbreak)
			{
				outbreakColor = "Black";
				setCubesLeft(getCubesLeft(0) - (3-cubesAlreadyThere), 0);
			}
			else
				setCubesLeft(getCubesLeft(0) - numberOfCubes, 0);
			System.out.println("Black cubes left: " + getCubesLeft(0));
		}
		else if (cubeColor == "Yellow" && getCubesLeft(1) >= numberOfCubes)
		{
			cubesAlreadyThere = cityList.get(indexOfCity).getYellowCubes();
			cityList.get(indexOfCity).setYellowCubes(cityList.get(indexOfCity).getYellowCubes() + numberOfCubes);
			outbreak = cityList.get(indexOfCity).checkOutbreak();
			if (outbreak)
			{
				outbreakColor = "Yellow";
				setCubesLeft(getCubesLeft(1) - (3-cubesAlreadyThere), 1);
			}
			else
				setCubesLeft(getCubesLeft(1) - numberOfCubes, 1);
			System.out.println("Yellow cubes left: " + getCubesLeft(1));
		}
		else if (cubeColor == "Blue" && getCubesLeft(2) >= numberOfCubes)
		{
			cubesAlreadyThere = cityList.get(indexOfCity).getBlueCubes();
			cityList.get(indexOfCity).setBlueCubes(cityList.get(indexOfCity).getBlueCubes() + numberOfCubes);
			outbreak = cityList.get(indexOfCity).checkOutbreak();
			if (outbreak)
			{
				outbreakColor = "Blue";
				setCubesLeft(getCubesLeft(2) - (3-cubesAlreadyThere), 2);
			}
			else
				setCubesLeft(getCubesLeft(2) - numberOfCubes, 2);
			System.out.println("Blue cubes left: " + getCubesLeft(2));
		}
		else if (cubeColor == "Red" && getCubesLeft(3) >= numberOfCubes)
		{
			cubesAlreadyThere = cityList.get(indexOfCity).getRedCubes();
			cityList.get(indexOfCity).setRedCubes(cityList.get(indexOfCity).getRedCubes() + numberOfCubes);
			outbreak = cityList.get(indexOfCity).checkOutbreak();
			if (outbreak)
			{
				outbreakColor = "Red";
				setCubesLeft(getCubesLeft(3) - (3-cubesAlreadyThere), 3);
			}
			else
				setCubesLeft(getCubesLeft(3) - numberOfCubes, 3);
			System.out.println("Red cubes left: " + getCubesLeft(3));
		}
		else
		{
			if (getCubesLeft(0) < numberOfCubes && cubeColor == "Black")
				System.out.println("\nYou are out of black cubes! You lost...\n");
			else if (getCubesLeft(1) < numberOfCubes && cubeColor == "Yellow")
				System.out.println("\nYou are out of yellow cubes! You lost...\n");
			else if (getCubesLeft(2) < numberOfCubes && cubeColor == "Blue")
				System.out.println("\nYou are out of blue cubes! You lost...\n");
			else if (getCubesLeft(3) < numberOfCubes && cubeColor == "Red")
				System.out.println("\nYou are out of red cubes! You lost...\n");
			
			gameEnded = true;
		}
		
		if (outbreak == true && gameEnded == false && !this.canNotOutbreak.contains(cityList.get(indexOfCity).getName()))
		{	
			this.canNotOutbreak.add(cityList.get(indexOfCity).getName());
			
			System.out.println("\nSOS --> Outbreak(" + outbreaksCount + ") in " + cityList.get(indexOfCity).getName() + " <-- SOS\n");
			outbreaksCount = outbreaksCount + 1;
			
			if (outbreaksCount >= outbreaksLimit) 
			{
				System.out.println("Max Outbreaks occured! You lost...\n");
				gameEnded = true;
			}
			else if (outbreaksCount < outbreaksLimit)
			{
				for (int i = 0 ; i < cityList.get(indexOfCity).getNeighboursNumber() ; i++)
					if (!this.canNotOutbreak.contains(cityList.get(indexOfCity).getNeighbour(i)))
						addCubes(cityList.get(indexOfCity).getNeighbour(i), 1, outbreakColor);
				
				outbreak = false;
				outbreakColor = "";
			}
		}
	}
	
	// Infect cities phase, also used in initial infection 
	public void infectCities(int numberOfCitiesToInfect, int amountOfCubesToAdd)
	{
		for(int j=0 ; j < numberOfCitiesToInfect ; j++)
		{
			if (gameEnded == false)
			{
				String cityToInfect = infectedDeck.get(0).toString();
				
				System.out.println("Infecting " + cityToInfect + "...");
				
				addCubes(cityToInfect, amountOfCubesToAdd, colorOf(cityToInfect));
				
				discardedPile.add(cityToInfect);
				infectedDeck.remove(0);
			}
		}

	}
	
	// Giving cards to desired player
	public void drawCards(int playerID, int numberOfCardsToDraw)
	{	
		if (isLimitSeven) 
		{
			if ((getHandOf(playerID).size() + 2) > 7)
			{
				numberOfCardsToDraw = 7 - getHandOf(playerID).size();
				System.out.println(usernames[playerID] + " will draw " + numberOfCardsToDraw + " cards instead of 2");
			}				
		}
		
		if (playersDeck.size() >= numberOfCardsToDraw)
		{
			for(int j=0 ; j < numberOfCardsToDraw ; j++)
			{
				String cardDrawn = playersDeck.get(0).toString();
				
				if (!cardDrawn.equals("Epidemic"))
				{
					System.out.println(usernames[playerID] + " just drawn the " + playersDeck.get(0).toString() + " card");
					//ystem.out.println("Giving " + playersDeck.get(0).toString() + " to " + usernames[playerID]);
					getHandOf(playerID).add(playersDeck.get(0).toString());
					playersDeck.remove(0);	
				}
				else
				{
					System.out.println("\nSOS --> EPIDEMIC IS DRAWN! <-- SOS\n");
					currentInfectionPointer = currentInfectionPointer + 1;
					
					String cityToInfect = infectedDeck.get(infectedDeck.size()- 1).toString();
					
					System.out.println("Infecting " + cityToInfect + " due to Epidemic");
					addCubes(cityToInfect, 3, colorOf(cityToInfect));
					
					discardedPile.add(cityToInfect);
					infectedDeck.remove(infectedDeck.size() - 1);
					
					Collections.shuffle(discardedPile);
					infectedDeck.addAll(0, discardedPile);
					discardedPile.clear();
					
					playersDeck.remove(0);
				}
			}
		}
		else
		{
			if (!checkIfWon())
				System.out.println("No more cards to draw! You lost...\n");
			gameEnded = true;
		}
	}
	
	// Building an RS in the desired city
	public void manualBuildResearchStation(String cityName)
	{
		if (this.RSCount < this.RSLimit)
		{
			boolean foundCity = false;
			
			for (City tmpCity : cityList)
			{	
				if (tmpCity.getName().equals(cityName))
				{
					foundCity = true;
					
					if (tmpCity.getHasReseachStation())
						System.out.println("This city has already an RS placed there...");
						
					else {
						tmpCity.setHasReseachStation(true);
						this.RSLocations.add(tmpCity.getName());
						this.RSCount = this.RSCount + 1;
						
						System.out.println("\nRS sucesfully placed in " +tmpCity.getName() + "\n");
					}	
				}
			}
			
			if (foundCity == false)
				System.out.println("Could not find the city you entered...");
		}
		else
			System.out.println("Already reached maximum RS limit...");
	}
	
	// Removing an RS from the desired city
	public void manualRemoveResearchStation(String cityName)
	{
		if (this.RSCount > 0)
		{
			boolean foundCity = false;
			
			for (City tmpCity : cityList)
			{	
				if (tmpCity.getName().equals(cityName))
				{
					foundCity = true;
					
					if (!tmpCity.getHasReseachStation())
						System.out.println("This city does not have an RS placed there...");
						
					else {
						tmpCity.setHasReseachStation(false);
						
						this.RSLocations.remove(this.RSLocations.indexOf(cityName));
						this.RSCount = this.RSCount - 1;
						
						System.out.println("RS sucesfully removed from " +tmpCity.getName());
					}	
				}
			}
			
			if (foundCity == false)
				System.out.println("Could not find the city you entered...");
		}
		else
			System.out.println("There is no RS to remove...");
	}
	
	// Check if eradicated
	public boolean checkIfEradicated(String colorOfDisease)
	{
		if (colorOfDisease == "Black")
			if (cured[0] == true &&  getCubesLeft(0) == 24)
				return true;
		else if (colorOfDisease == "Yellow")
			if (cured[1] == true &&  getCubesLeft(1) == 24)
				return true;
		else if (colorOfDisease == "Blue")
			if (cured[2] == true &&  getCubesLeft(2) == 24)
				return true;
		else if (colorOfDisease == "Red")
			if (cured[3] == true &&  getCubesLeft(3) == 24)
				return true;
		
		return false;
		
		}
	
	// Check if won 
	public boolean checkIfWon()
	{
		boolean won = true;
		
		for (int i = 0 ; i < 4 ; i++)
			if (getCured(i) == false)
				won = false;
		
		return won;
	}
	
	
	// Players actions
	
	// Drive / Ferry to the desired city
	public boolean driveTo(int playerID, String destination)
	{	
		City currentCity = searchForCity(getPawnsLocations(playerID));
		boolean isLegal = false;
		
		for (int i = 0 ; i < currentCity.getNeighboursNumber() ; i++)
			if (currentCity.getNeighbour(i).equals(destination))
				isLegal = true;
		
		if (isLegal) {
			System.out.println(usernames[playerID] + " is driving / getting ferry to " + destination + " from " + currentCity.getName());
			setPawnsLocations(playerID, destination);
			return true;
		}
		else {
			System.out.println(destination + " is not accesible by drive / ferry from " + currentCity.getName());
			return false;
		}
	}
	
	// Direct flight to desired city
	public boolean directFlight(int playerID, String destination)
	{
		boolean isLegal = false;
		
		if (getHandOf(playerID).contains(destination))
			isLegal = true;
		
		if (isLegal) {
			System.out.println(usernames[playerID] + " is getting a direct flight to " + destination + " from " + getPawnsLocations(playerID));
			setPawnsLocations(playerID, destination);
			getHandOf(playerID).remove(destination);
			return true;
		}
		else {
			System.out.println(destination + " is not accessible by direct flight");
			return false;
		}
	}
	
	// Charter flight to desired city
	public boolean charterFlight(int playerID, String destination)
	{
		boolean isLegal = false;
		
		if (getHandOf(playerID).contains(getPawnsLocations(playerID)))
			isLegal = true;
		
		if (isLegal) {
			System.out.println(usernames[playerID] + " is getting a charter flight to " + destination + " from " + getPawnsLocations(playerID));
			setPawnsLocations(playerID, destination);
			getHandOf(playerID).remove(getPawnsLocations(playerID));
			return true;
		}
		else {
			System.out.println(destination + " is not accessible by charter flight");
			return false;
		}
	}
	
	// Shuttle flight to desired city
	public boolean shuttleFlight(int playerID, String destination)
	{
		boolean isLegal = false;
		
		if (RSLocations.contains(getPawnsLocations(playerID)) && RSLocations.contains(destination))
			isLegal = true;
		
		if (isLegal) {
			System.out.println(usernames[playerID] + " is getting a shuttle flight to " + destination + " from " + getPawnsLocations(playerID));
			setPawnsLocations(playerID, destination);
			return true;
		}
		else {
			System.out.println(destination + " is not accessible by shuttle flight");
			return false;
		}
	}
	
	// Build Research Station to specific city
	public boolean buildRS(int playerID, String cityToBuild)
	{
		boolean isOperationsExpert = getRoleOf(playerID).equals("Operations Expert");
		boolean isLegal = false;
		
		if(getPawnsLocations(playerID).equals(cityToBuild) && isOperationsExpert)
			isLegal = true;
		else if(getPawnsLocations(playerID).equals(cityToBuild) && getHandOf(playerID).contains(cityToBuild))
			isLegal = true;
		else 
			isLegal = false;
		
		if (isLegal && isOperationsExpert) {
			System.out.println(usernames[playerID] + " is building an RS in " + cityToBuild + " as the operations expert");
			RSLocations.add(cityToBuild);
			return true;
		}
		else if (isLegal) {
			System.out.println(usernames[playerID] + " is building an RS in " + cityToBuild);
			RSLocations.add(cityToBuild);
			getHandOf(playerID).remove(cityToBuild);
			return true;
		}
		else {
			System.out.println("Could not build an RS in " + cityToBuild);
			return false;
		}
	}
	
	public boolean removeRS(int playerID, String cityToRemove)
	{
		
		if (getRSLocations().contains(cityToRemove))
		{
			System.out.println(usernames[playerID] + " is removing the Reseach Station from " + cityToRemove);
			manualRemoveResearchStation(cityToRemove);
			return true;
		}
		else 
		{
			System.out.println("There is no RS there to remove..");
			return false;
		}
	}
	
	// treat a disease action
	public boolean treatDisease(int playerID, String cityToTreat, String color)
	{
		City currentCity = searchForCity(getPawnsLocations(playerID));
		boolean isMedic = getRoleOf(playerID).equals("Medic");
		int cubesRemoved = 0;
		boolean isLegal = false;
		
		if(getPawnsLocations(playerID).equals(cityToTreat))
		{
			if (color.equals("Black") && currentCity.getBlackCubes() > 0)
				isLegal = true;
			else if (color.equals("Yellow") && currentCity.getYellowCubes() > 0)
				isLegal = true;
			else if (color.equals("Blue") && currentCity.getBlueCubes() > 0)
				isLegal = true;
			else if (color.equals("Red") && currentCity.getRedCubes() > 0)
				isLegal = true;
		}
		
		if (isMedic) {
			
			if (isLegal && color.equals("Black"))
				{
				cubesRemoved = currentCity.getBlackCubes();
				cubesLeft[0] =cubesLeft[0]+cubesRemoved;
				currentCity.setBlackCubes(0);
				}
			else if (isLegal && color.equals("Yellow"))
				{
				cubesRemoved = currentCity.getYellowCubes();
				cubesLeft[1] =cubesLeft[1]+cubesRemoved;
				currentCity.setYellowCubes(0);
				}
			else if (isLegal && color.equals("Blue"))
				{
				cubesRemoved = currentCity.getBlueCubes();
				cubesLeft[2] =cubesLeft[2]+cubesRemoved;
				currentCity.setBlueCubes(0);
				}
			else if (isLegal && color.equals("Red"))
				{
				cubesRemoved = currentCity.getRedCubes();
				cubesLeft[3] =cubesLeft[3]+cubesRemoved;
				currentCity.setRedCubes(0);
				}
		}
		else {
			cubesRemoved = 1;
			
			if (isLegal && color.equals("Black")) {
				cubesLeft[0] =cubesLeft[0]+cubesRemoved;
				currentCity.setBlackCubes(currentCity.getBlackCubes() - 1);
			}
			else if (isLegal && color.equals("Yellow")) {
				cubesLeft[1] =cubesLeft[1]+cubesRemoved;
				currentCity.setYellowCubes(currentCity.getYellowCubes() - 1);
			}
			else if (isLegal && color.equals("Blue")) {
				cubesLeft[2] =cubesLeft[2]+cubesRemoved;
				currentCity.setBlueCubes(currentCity.getBlueCubes() - 1);
			}
			else if (isLegal && color.equals("Red")) {
				cubesLeft[3] =cubesLeft[3]+cubesRemoved;
				currentCity.setRedCubes(currentCity.getRedCubes() - 1);
			}
		}
		
		if (isLegal && isMedic) {
			System.out.println(usernames[playerID] + " treated all (" + cubesRemoved + ") " + color + " cube(s) from " + cityToTreat + " as the Medic");
			return true;
		}
		else if (isLegal && !isMedic) {
			System.out.println(usernames[playerID] + " treated 1 " + color + " cube from " + cityToTreat);
			return true;
		}
		else {
			System.out.println(getUsernames(playerID) + " could not treat " + cityToTreat);
			return false;
		}
	}
	
	// cure disease action
	public boolean cureDisease(int playerID, String colorToCure)
	{
		boolean isScientist = getRoleOf(playerID).equals("Scientist");
		int cardsColorCount = 0;
		int cardsDiscarted = 0;
		boolean isLegal = false;
		
		if (getRSLocations().contains(getPawnsLocations(playerID)))
		{
			for (int i = 0 ; i < getHandOf(playerID).size() ; i++)
			{
				City cityToCheck = searchForCity(getHandOf(playerID).get(i));
				
				if (cityToCheck.getColour().equals(colorToCure))
					cardsColorCount ++;
			}
			
			if (cardsColorCount >= (cardsNeededForCure-1) && isScientist)
				isLegal = true;
			else if (cardsColorCount >= cardsNeededForCure)
				isLegal = true;
			else 
				isLegal = false;
			
			if (isLegal)
			{
				
				if (isScientist) {
					System.out.println(usernames[playerID] + " cured the " + colorToCure + " disease as the scientist!");
				}
				else {
					System.out.println(usernames[playerID] + " cured the " + colorToCure + " disease!");
					
				}
				
				setCured(true, colorToCure);
				
				//for (int i = 0 ; i < getHandOf(playerID).size() ; i++)
				for (int i = (getHandOf(playerID).size() - 1) ; i > -1  ; i--)
				{
					City cityToCheck = searchForCity(getHandOf(playerID).get(i));
					
					if (cityToCheck.getColour().equals(colorToCure))
					{
						getHandOf(playerID).remove(cityToCheck.getName());
						cardsDiscarted ++;
						
						if (cardsDiscarted == (cardsNeededForCure-1) && isScientist)
							break;
						else if (cardsDiscarted == cardsNeededForCure)
							break;
					}
				}
				return true;
			}
			else
			{
				System.out.println("You don't have enought cards to cure...");
				return false;
			}
			
		}
		else {
			System.out.println("You must be in a RS to cure...");
			return false;
		}
	}
	
	public boolean cureDisease(int playerID, String colorToCure, String card1, String card2, String card3, String card4)
	{
		boolean isScientist = getRoleOf(playerID).equals("Scientist");
		int cardsColorCount = 0;
		boolean isLegal = false;
		
		if (getRSLocations().contains(getPawnsLocations(playerID)))
		{
			for (int i = 0 ; i < getHandOf(playerID).size() ; i++)
			{
				City cityToCheck = searchForCity(getHandOf(playerID).get(i));
				
				if (cityToCheck.getColour().equals(colorToCure))
					cardsColorCount ++;
			}
			
			if (cardsColorCount >= (cardsNeededForCure-1) && isScientist)
				isLegal = true;
			else if (cardsColorCount >= cardsNeededForCure)
				isLegal = true;
			else 
				isLegal = false;
			
			if (isLegal)
			{
				
				if (isScientist)
					System.out.println(usernames[playerID] + " cured the " + colorToCure + " disease as the scientist!");
				else
					System.out.println(usernames[playerID] + " cured the " + colorToCure + " disease!");
				
				setCured(true, colorToCure);
				
				getHandOf(playerID).remove(card1);
				getHandOf(playerID).remove(card2);
				getHandOf(playerID).remove(card3);
				
				if (!isScientist) {
					getHandOf(playerID).remove(card4);
				}
				
				return true;
			}
			else
			{
				System.out.println("You don't have enought cards to cure...");
				return false;
			}
			
		}
		else {
			System.out.println("You must be in a RS to cure...");
			return false;
		}
	}
	
	
	public void actionPass(int playerID)
	{
		System.out.println(usernames[playerID] + " decided not to use this action..");
	}
	
	public void chatMessage(int playerID, String messageToSend)
	{
		System.out.println(usernames[playerID] + ": " + messageToSend);
	}
	
	//  --> Special abilities functions <--
	
	// Quarantine specialist special protection ability
	public void updateQuarantineSpecialistProtectionList()
	{
		int quarSpID = 5;
		String currentCityString;
		City currentCity;
		
		
		for (int i = 0 ; i < numberOfPlayers ; i++)
			if (getRoleOf(i).equals("Quarantine Specialist"))
				quarSpID = i;
		
		underQuarantineSpecialistProtection.clear();
		
		if (quarSpID == 5)
			return;
		
		currentCityString = getPawnsLocations(quarSpID);
		currentCity = searchForCity(currentCityString);
		
		underQuarantineSpecialistProtection.add(currentCityString);
		
		for (int j = 0 ; j < currentCity.getNeighboursNumber() ; j++)
			underQuarantineSpecialistProtection.add(currentCity.getNeighbour(j));
	}
	
	// Medic special protection ability
	public void updateMedicProtectionList(String colorOfDiasease)
	{
		int medID = 5;
		String currentCityString;
		
		for (int i = 0 ; i < numberOfPlayers ; i++)
			if (getRoleOf(i).equals("Medic"))
				medID = i;
		
		underMedicProtection.clear();
		
		if (medID == 5)
			return;
		
		if (colorOfDiasease == "Black" && cured[0] == false)
			return;
		else if (colorOfDiasease == "Yellow" && cured[1] == false)
			return;
		else if (colorOfDiasease == "Blue" && cured[2] == false)
			return;
		else if (colorOfDiasease == "Red" && cured[3] == false)
			return;
		
		currentCityString = getPawnsLocations(medID);
		
		underMedicProtection.add(currentCityString);
	} 
	
	// Medic special cure ability
	public void checkMedicSpecialAbility(int playerID) {
		
		if (!getRoleOf(playerID).equals("Medic"))
			return;
		
		String currentCityString = getPawnsLocations(playerID);
		City currentCity = searchForCity(currentCityString);
		
		if (cured[0] == true)
		{
			int blackCubesThere = currentCity.getBlackCubes();
			
			if (blackCubesThere != 0)
			{
				setCubesLeft(getCubesLeft(0) + blackCubesThere, 0);
				currentCity.setBlackCubes(0);
				System.out.println("Medic just removed " + blackCubesThere + " black cube(s) from " + currentCityString + " with his special cure ability cause this disease is already cured!");
			}
		}
		
		if (cured[1] == true)
		{
			int yellowCubesThere = currentCity.getYellowCubes();
			
			if (yellowCubesThere != 0)
			{
				setCubesLeft(getCubesLeft(1) + yellowCubesThere, 1);
				currentCity.setYellowCubes(0);
				System.out.println("Medic just removed " + yellowCubesThere + " yellow cube(s) from " + currentCityString + " with his special cure ability cause this disease is already cured!");
			}
		}
		
		if (cured[2] == true)
		{
			int blueCubesThere = currentCity.getBlueCubes();
			
			if (blueCubesThere != 0)
			{
				setCubesLeft(getCubesLeft(2) + blueCubesThere, 2);
				currentCity.setBlueCubes(0);
				System.out.println("Medic just removed " + blueCubesThere + " blue cube(s) from " + currentCityString + " with his special cure ability cause this disease is already cured!");
			}
		}
		
		if (cured[3] == true)
		{
			int redCubesThere = currentCity.getRedCubes();
			
			if (redCubesThere != 0)
			{
				setCubesLeft(getCubesLeft(3) + redCubesThere, 3);
				currentCity.setRedCubes(0);
				System.out.println("Medic just removed " + redCubesThere + " red cube(s) from " + currentCityString + " with his special cure ability cause this disease is already cured!");
			}
		}
		
	}
	
	
	// Operations Expert special travel ability
	public void operationsExpertTravel(int playerID, String destination, String cardToThrow)
	{
		int operSpID = 5;
		String currentCiyString;
		boolean isLegal = false;
		
		for (int i = 0 ; i < numberOfPlayers ; i++)
			if (getRoleOf(i).equals("Operations Expert"))
				operSpID = i;
		
		if (operSpID == 5)
			return;
		else if (operSpID != playerID)
			return;
		
		currentCiyString = getPawnsLocations(operSpID);
		
		if(RSLocations.contains(currentCiyString) && !getHandOf(operSpID).isEmpty())
			isLegal = true;
			
		if (isLegal) {
			System.out.println(usernames[operSpID] + " is getting a special Operations Expert shuttle flight to " + destination + " from " + currentCiyString + " using the card of " + cardToThrow);
			setPawnsLocations(operSpID, destination);
			getHandOf(operSpID).remove(cardToThrow);
		}
		else
			System.out.println(destination + " is not accessible by special Operations Expert shuttle flight");
	}
	
	
	//  --> Functions that are used from the Server <--
	
	// Resets the global message from Server
	public void resetAllMessages() 
	{
		this.messageToAllClients = "";
		this.messageToClient[0] = "";
		this.messageToClient[1] = "";
		this.messageToClient[2] = "";
		this.messageToClient[3] = "";
	}
	
	// Resets the personalized messages from Server
	public void resetPersonalizedMessages() 
	{
		this.messageToClient[0] = "";
		this.messageToClient[1] = "";
		this.messageToClient[2] = "";
		this.messageToClient[3] = "";
	}
	
	// Resets the talkedForThisRound variable for all clients
	public void resetTalkedForThisTurn() 
	{
		this.talkedForThisTurn[0] = false;
		this.talkedForThisTurn[1] = false;
		this.talkedForThisTurn[2] = false;
		this.talkedForThisTurn[3] = false;
	}

	// Resets the talkedForThisRound variable for all clients
	public void resetCanNotOutbreak() 
	{
		this.canNotOutbreak.clear();
	}
	
	// --> Getters & Setters <--
	
	// Getters & Setters for "final" variables
	
	public String getRoleOf(int playerID) {
		return this.roles[playerID];
	}
	
	public void setNumberOfPlayers(int numberOfPlayers) {
		this.numberOfPlayers = numberOfPlayers;
	}
	
	public int getResearchStationsLimit() {
		return RSLimit;
	}
	
	// Getters & Setters for normal variables
	
	public String getUsernames(int playerID) {
		return usernames[playerID];
	}

	public void setUsernames(String username, int playerID) {
		this.usernames[playerID] = username;
	}

	public void setGameEnded(boolean gameEnded) {
		this.gameEnded = gameEnded;
	}
	
	public boolean getGameEnded() {
		return this.gameEnded;
	}
	
	public boolean getTalkedForThisTurn(int playerID) {
		return talkedForThisTurn[playerID];
	}

	public void setTalkedForThisTurn(boolean talkedForThisTurn, int playerID) {
		this.talkedForThisTurn[playerID] = talkedForThisTurn;
	}

	public void setRound(int round) {
		this.round = round;
	}
	
	public int getRound() {
		return this.round;
	}

	public void setWhoIsPlaying(int playerID) {
		this.whoIsPlaying = playerID;
	}
	
	public int getWhoIsPlaying() {
		return this.whoIsPlaying;
	}
		
	public int getWhoIsTalking() {
		return whoIsTalking;
	}

	public void setWhoIsTalking(int whoIsTalking) {
		this.whoIsTalking = whoIsTalking;
	}

	public void setMessageToAllClients(String message) {
		this.messageToAllClients = message;
	}

	public String getMessageToAllClients() {
		return this.messageToAllClients;
	}
	
	public void setMessageToClient(String message, int clientID) {
		this.messageToClient[clientID] = message;
	}

	public String getMessageToClient(int clientID) {
		return this.messageToClient[clientID];
	}
	
	public void setCubesLeft(int cubes, int colorID) {
		this.cubesLeft[colorID] = cubes;
	}
	
	public int getCubesLeft(int colorID) {
		return this.cubesLeft[colorID];
	}
	
	public void setCured(boolean cured, String color) {
		
		if (color.equals("Black"))
			this.cured[0] = cured;
		else if (color.equals("Yellow"))
			this.cured[1] = cured;
		else if (color.equals("Blue"))
			this.cured[2] = cured;
		else if (color.equals("Red"))
			this.cured[3] = cured;
		
	}

	public boolean getCured(int colorID) {
		return this.cured[colorID];
	}
	
	public boolean getCured(String color) 
	{
	if (color.equals("Black"))
		return cured[0];
	else if (color.equals("Yellow"))
		return cured[1];
	else if (color.equals("Blue"))
		return cured[2];
	else if (color.equals("Red"))
		return cured[3];
	else 
		return false;
	}

	public boolean getErradicated(int colorID) {
		return erradicated[colorID];
	}

	public void setErradicated(boolean erradicated, int colorID) {
		this.erradicated[colorID] = erradicated;
	}

	public String getActions(int playerID) {
		return actions[playerID];
	}

	public void setActions(String action, int playerID) {
		this.actions[playerID] = action;
	}

	public String getPawnsLocations(int playerID) {
		return pawnsLocations[playerID];
	}

	public void setPawnsLocations(int playerID, String pawnLocation) {
		this.pawnsLocations[playerID] = pawnLocation;
		
		checkMedicSpecialAbility(playerID);
	}

	public int getResearchStationsBuild() {
		return RSCount;
	}

	public void setResearchStationsBuild(int RSCount) {
		this.RSCount = RSCount;
	}

	public int getOutbreaksCount() {
		return outbreaksCount;
	}

	public void setOutbreaksCount(int outbreaksCount) {
		this.outbreaksCount = outbreaksCount;
	}

	public int getInfectionRate() {
		return this.infectionRate[currentInfectionPointer];
	}

	public int getCardsNeededForCure() {
		return cardsNeededForCure;
	}
	
	public void setInfectionPointer(int currentInfectionPointer) {
		this.currentInfectionPointer = currentInfectionPointer;
	}

	public ArrayList<String> getRSLocations() {
		return RSLocations;
	}

	public boolean getIsQuietNight() {
		return isQuietNight;
	}

	public void setIsQuietNight(boolean isQuietNight) {
		this.isQuietNight = isQuietNight;
	}

	public String getColors(int colorID) {
		return colors[colorID];
	}
	
	public int getNumberOfEpidemicCards() {
		return numberOfEpidemicCards;
	}
	
	public int getNumberOfEventCards() {
		return numberOfEventCards;
	}
	
	public ArrayList<String> getPlayersDeck() {
		return playersDeck;
	}
	
	public ArrayList<String> getInfectedDeck() {
		return infectedDeck;
	}
	
	public ArrayList<String> getDiscardedPile() {
		return discardedPile;
	}
	
	
}
