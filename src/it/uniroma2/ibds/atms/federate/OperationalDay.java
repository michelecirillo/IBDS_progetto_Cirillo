package it.uniroma2.ibds.atms.federate;

import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import it.uniroma2.ibds.atms.scenario.Airplane;

public class OperationalDay {
	private Date date;
	private SortedMap<Long, Airplane> flightsScheduled;

	public OperationalDay(Date date) {
		if (date.after(new Date())) {
			throw new IllegalArgumentException("Inserita data precedente ad oggi!");
		}
		this.date = date;
		this.flightsScheduled = new TreeMap<Long, Airplane>();
	}

	public Date getDate() {
		return date;
	}

	public void scheduleNewFlight(long time, Airplane a) {
		this.flightsScheduled.put(time, a);
	}

	public Iterator<Airplane> getAirplanesScheduled() {
		return this.flightsScheduled.values().iterator();
	}

	public void removeFlight(Airplane a) {
		this.flightsScheduled.remove(a);
	}

	public int length() {
		return this.flightsScheduled.size();
	}
	
	public SortedMap<Long, Airplane> getFlightsScheduled() {
		return this.flightsScheduled;
	}

	/**
	 * Utile per ordinare gli operational day in base alla data
	 */
	@Override
	public int hashCode() {
		return Objects.hash(date);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperationalDay other = (OperationalDay) obj;
		return Objects.equals(date, other.date);
	}
	
	/**
	 * 
	 * Update typed allowed on this HLA Object Class
	 *
	 */
	static enum UpdateType{
		INSERT
	}

}
