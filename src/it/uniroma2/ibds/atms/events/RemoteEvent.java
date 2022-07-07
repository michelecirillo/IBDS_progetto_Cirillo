package it.uniroma2.ibds.atms.events;

import it.uniroma2.ibds.atms.scenario.Airplane;

public class RemoteEvent extends Event {

	private Airplane airplane;

	public RemoteEvent(EventType type, Long time, Airplane airplane) {
		super(type, time);
		this.airplane = airplane;
	}

	public Airplane getAirplane() {
		return airplane;
	}

	public void setAirplane(Airplane airplane) {
		this.airplane = airplane;
	}
	
}
