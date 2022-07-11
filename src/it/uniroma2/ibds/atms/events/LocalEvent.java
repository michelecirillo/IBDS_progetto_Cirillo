package it.uniroma2.ibds.atms.events;

public abstract class LocalEvent extends Event implements Comparable<LocalEvent>{

	public LocalEvent(EventType type, Long time) {
		super(type, time);
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
