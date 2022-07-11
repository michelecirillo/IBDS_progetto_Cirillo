package it.uniroma2.ibds.atms.events;

import it.uniroma2.ibds.atms.scenario.Airplane;

public class AirplaneEvent extends LocalEvent implements Comparable<LocalEvent>{
	
	private Airplane airplane; //airplane generating/affected by the event
	
	public AirplaneEvent(EventType eventType, Long time, Airplane airplane) {
		super(eventType, time);
		this.airplane = airplane;
	}

	/**
	 * @return the airplane
	 */
	public Airplane getAirplane() {
		return airplane;
	}

	/**
	 * @param airplane the airplane to set
	 */
	public void setAirplane(Airplane airplane) {
		this.airplane = airplane;
	}
	
	@Override
	public int compareTo(LocalEvent o) {
		if(this.getTime() == o.getTime()) 
			return 0; 
		else if (this.getTime() > o.getTime())
			return 1;
		else return -1;
		
	}
	

}
