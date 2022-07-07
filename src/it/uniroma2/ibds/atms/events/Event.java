package it.uniroma2.ibds.atms.events;

public abstract class Event {
	private EventType type; //enum denoting the event type
	private Long time; //event logical time
	
	public Event(EventType type, Long time) {
		this.type = type;
		this.time = time;
	}
	
	/**
	 * @return the eventType
	 */
	public EventType getEventType() {
		return type;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(EventType eventType) {
		this.type = eventType;
	}

	/**
	 * @return the time
	 */
	public Long getTime() {
		return time;
	}

	/**
	 * @param timeStamp the time to set
	 */
	public void setTime(Long time) {
		this.time = time;
	}
	
}
