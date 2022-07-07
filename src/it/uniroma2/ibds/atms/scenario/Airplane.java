package it.uniroma2.ibds.atms.scenario;

import java.util.Objects;

public class Airplane {

	AirplaneState state;
	String flightCode;
	String airport;
	String destinationAirport;
	private int travelTime;

	public Airplane(AirplaneState state, String flightCode, String airport, String destinationAirport, int travelTime) {
		this.state = state;
		this.flightCode = flightCode;
		this.airport = airport;
		this.destinationAirport = destinationAirport;
		this.travelTime = travelTime;
	}

	/**
	 * @return the status
	 */
	public AirplaneState getState() {
		return state;
	}

	/**
	 * @param status the status to set
	 */
	public void setState(AirplaneState state) {
		this.state = state;
	}

	/**
	 * @return the flightCode
	 */
	public String getFlightCode() {
		return flightCode;
	}

	/**
	 * @param flightCode the flightCode to set
	 */
	public void setFlightCode(String flightCode) {
		this.flightCode = flightCode;
	}

	public String getAirport() {
		return airport;
	}

	public void setAirport(String airport) {
		this.airport = airport;
	}

	public String getDestinationAirport() {
		return destinationAirport;
	}

	public void setDestinationAirport(String destinationAirport) {
		this.destinationAirport = destinationAirport;
	}

	public int getTravelTime() {
		return travelTime;
	}

	public void setTravelTime(int travelTime) {
		this.travelTime = travelTime;
	}

	/**
	 * Needed for identifying airplanes using their flightCode 
	 */
	@Override
	public int hashCode() {
		return Objects.hash(flightCode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Airplane other = (Airplane) obj;
		return Objects.equals(flightCode, other.flightCode);
	}

}
