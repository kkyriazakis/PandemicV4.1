package PLH512.client;

import PLH512.server.City;

public class citiesWithDistancesObj {

	private String name;
	private City cityObj;
	private int distance;
	
	public citiesWithDistancesObj (String name, City cityObj, int distance)
	{
		this.setName(name);
		this.setCityObj(cityObj);
		this.setDistance(distance);
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public City getCityObj() {
		return cityObj;
	}

	private void setCityObj(City cityObj) {
		this.cityObj = cityObj;
	}

	public int getDistance() {
		return distance;
	}

	private void setDistance(int distance) {
		this.distance = distance;
	}
}
