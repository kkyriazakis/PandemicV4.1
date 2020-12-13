package PLH512.server;

import java.io.Serializable;

@SuppressWarnings("serial")
public class City implements Serializable{

	private String name;
	private String colour;
	
	private int neighboursNumber;
	private String[] neighbourCities = new String[6] ;
	
	private int blackCubes;
	private int yellowCubes;
	private int blueCubes;
	private int redCubes;
	
	private boolean hasReseachStation;
	
	public int getMaxCube()
	{
		if (blackCubes >= yellowCubes && blackCubes >= blueCubes && blackCubes >= redCubes)
			return blackCubes;
		else if (yellowCubes >= blueCubes && yellowCubes >= redCubes)
			return yellowCubes;
		else if (blueCubes >= redCubes)
			return blueCubes;
		else 
			return redCubes;
	}
	
	public String getMaxCubeColor()
	{
		if (blackCubes >= yellowCubes && blackCubes >= blueCubes && blackCubes >= redCubes)
			return "Black";
		else if (yellowCubes >= blueCubes && yellowCubes >= redCubes)
			return "Yellow";
		else if (blueCubes >= redCubes)
			return "Blue";
		else 
			return "Red";
	}
	
	public void removeCube(String cubeColor)
	{
		if (cubeColor.equals("Black") && blackCubes > 0)
			blackCubes--;
		else if (cubeColor.equals("Yellow") && yellowCubes > 0)
			yellowCubes--;
		else if (cubeColor.equals("Blue") && blueCubes > 0)
			blueCubes--;
		else if (cubeColor.equals("Red") && redCubes > 0)
			redCubes--;
	}
	
	public void addCube(String cubeColor)
	{
		if (cubeColor.equals("Black") && blackCubes > 0)
			blackCubes++;
		else if (cubeColor.equals("Yellow") && yellowCubes > 0)
			yellowCubes++;
		else if (cubeColor.equals("Blue") && blueCubes > 0)
			blueCubes++;
		else if (cubeColor.equals("Red") && redCubes > 0)
			redCubes++;
	}
	
	public boolean checkOutbreak ()
	{
		if (this.redCubes >= 4) 
		{
			this.redCubes = 3;
			return true; 
		}
        else if (this.blueCubes >= 4) 
        {
        	this.blueCubes = 3;
        	return true; 
    	}
        else if (this.yellowCubes >= 4) 
        {
        	this.yellowCubes = 3;
        	return true; 
    	}
        else if (this.blackCubes >= 4) 
        {
        	this.blackCubes = 3;
        	return true; 
        }
        else
        	return false;
	}
	
	public int getCubes(String colour) {
		if (colour.equals("Red"))
            return redCubes;
        else if (colour.equals("Blue"))
            return blueCubes;
        else if (colour.equals("Yellow"))
            return yellowCubes;
        else if (colour.equals("Black"))
            return blackCubes;
        else
        	return 0;
	}
	
	// Constructor
	public City(String name, String colour, int nn, String n0, String n1, String n2, String n3, String n4, String n5) 
	{
		this.name = name;
		this.colour = colour;
		this.neighboursNumber = nn;
		this.neighbourCities[0] = n0;
		this.neighbourCities[1] = n1;
		this.neighbourCities[2] = n2;
		this.neighbourCities[3] = n3;
		this.neighbourCities[4] = n4;
		this.neighbourCities[5] = n5;
	}
	
	// Getters & Setters for "final" variables

	public String getName() {
		return name;
	}
	
	public String getColour() {
		return colour;
	}
	
	public int getNeighboursNumber() {
		return neighboursNumber;
	}
	
	public String getNeighbour(int neighbourID) {
		return neighbourCities[neighbourID];
	}
	
	// Getters & Setters for normal variables

	public int getBlackCubes() {
		return blackCubes;
	}

	public void setBlackCubes(int blackCubes) {
		this.blackCubes = blackCubes;
	}

	public int getYellowCubes() {
		return yellowCubes;
	}

	public void setYellowCubes(int yellowCubes) {
		this.yellowCubes = yellowCubes;
	}

	public int getBlueCubes() {
		return blueCubes;
	}

	public void setBlueCubes(int blueCubes) {
		this.blueCubes = blueCubes;
	}

	public int getRedCubes() {
		return redCubes;
	}

	public void setRedCubes(int redCubes) {
		this.redCubes = redCubes;
	}

	public boolean getHasReseachStation() {
		return hasReseachStation;
	}

	public void setHasReseachStation(boolean hasReseachStation) {
		this.hasReseachStation = hasReseachStation;
	}
	
}
