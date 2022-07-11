package it.uniroma2.ibds.atms.events;

public class RunwayEvent extends LocalEvent {
	
	int runway;
	boolean clearance;

	public RunwayEvent(EventType type, Long time, int runway, boolean clearance) {
		super(type, time);
		this.runway = runway;
		this.clearance = clearance;
	}

	public int getRunway() {
		return runway;
	}

	public boolean isClearance() {
		return clearance;
	}
	
}
