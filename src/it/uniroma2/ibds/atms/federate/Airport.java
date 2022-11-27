package it.uniroma2.ibds.atms.federate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedMap;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;
import it.uniroma2.ibds.atms.events.AirplaneEvent;
import it.uniroma2.ibds.atms.events.EventType;
import it.uniroma2.ibds.atms.events.LocalEvent;
import it.uniroma2.ibds.atms.events.RunwayEvent;
import it.uniroma2.ibds.atms.scenario.Airplane;
import it.uniroma2.ibds.atms.scenario.AirplaneState;

/**
 * 
 * In this version, each airport has an OperationalDay HLAObjectClass that can
 * generate a new event: both local and remote to the other airport
 * 
 */

public class Airport {

	// Messaggio di errore in caso venga inserita una data precedente ad oggi
	static final String DATE_MESSAGE_EXCEPTION = "Inserita data precedente ad oggi!";

	// Airport properties
	private String code; // airport code
	private Boolean[] isRunwayClear; // runways clearance state
	private Set<Airplane> managedAirplanes; // collection of airplanes under the Control Tower
											// responsibility
	private PriorityQueue<LocalEvent> eventsList; // ordered queue of events to be processed (PriorityQueue allows
													// duplicates)
	private OperationalDay operationalDay;

	private long _simulationEndTime;

	// HLA-related properties
	protected RTIambassador rtiAmb;
	protected final String FEDERATION_NAME = "ATMS Simulation";
	protected FederateAmbassadorImpl fedAmbassador;
	protected String federateName;
	protected HLAinteger64TimeFactory timeFactory; // set when we join
	protected EncoderFactory encoderFactory;

	// handles types - set once we join a federation
	protected ObjectClassHandle ocOperationalDayHandle;
	protected AttributeHandle flightsScheduledHandle;
	protected ObjectInstanceHandle instanceODHandle;
	protected AttributeHandleSet attributes;

	public Airport(String code, int runwaysLength, OperationalDay operationalDay, long endTime) {
		this.code = code;
		this.managedAirplanes = new HashSet<Airplane>();
		this.eventsList = new PriorityQueue<LocalEvent>();
		this.federateName = code + "_Airport";
		this._simulationEndTime = endTime;
		this.operationalDay = operationalDay;
		this.isRunwayClear = new Boolean[runwaysLength];
	}

	/**
	 * Schedule new flight to operational day list and update HLA Object Class
	 * 
	 * @param nextEventTime arrival/departure time
	 * @param a             airplane affected
	 */
	public void scheduleNewFlight(long nextEventTime, Airplane a) {
		this.operationalDay.scheduleNewFlight(nextEventTime, a);
		this.notifyOperationalDayInsert(nextEventTime, a);
	}

	/**
	 * Only add a flight to the operational day list
	 * 
	 * @param time arrival/departure time
	 * @param a    airplane affected
	 */
	public void addFlightToOperationalDay(long time, Airplane a) {
		this.operationalDay.scheduleNewFlight(time, a);
	}

	/**
	 * Add new event to the local events list
	 * 
	 * @param e event to add
	 */
	public void addEvent(LocalEvent e) {
		this.eventsList.add(e);
	}

	/**
	 * Get next event from the queue
	 * 
	 * @return next event to be processed
	 */
	private LocalEvent getNextEvent() {
		return this.eventsList.peek();
	}

	/**
	 * Add new airplane to the airport managed airplanes
	 * 
	 * @param a airplane to be added
	 */
	public void addManagedAirplane(Airplane a) {
		this.managedAirplanes.add(a);
	}

	/**
	 * @return federation name
	 */
	public String getFederateName() {
		return federateName;
	}

	/**
	 * @return airport code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * 
	 * @return number of runways of the airport
	 */
	public int getNumberOfRunways() {
		return this.isRunwayClear.length;
	}

	/**
	 * @param num number of runway that we want check the clearance
	 * @return runway num status
	 */
	public boolean isRunwayClear(int num) {
		if (num >= isRunwayClear.length)
			throw new IllegalArgumentException("L'aeroporto ha un numero di piste inferiori");
		return isRunwayClear[num];
	}

	/**
	 * @return index of a clear runway, or -1 is every runway is busy
	 */
	public int runwayClear() {
		for (int i = 0; i < isRunwayClear.length; i++) {
			if (isRunwayClear[i])
				return i;
		}
		return -1;
	}

	/**
	 * @param index runway index that we want set status
	 * @param state to be set in runway selected
	 */
	public void setRunwayClearance(int index, boolean state) {
		this.isRunwayClear[index] = state;
	}

	public void startFederate(String host) {

		LocalEvent event;
		long nextEventTimestamp; // timestamp of the next event to be processed
		long currentTime; // current logical time
		long nextTime; // timestamp of the next event to be scheduled
		long timeStep = 10; // time step used for determining the nextTime value

		// Federate Initialization
		initFederate(host);

		// ---------- Simulation Main Loop ---------------
		System.out.println("\n" + "___________________________________________");
		System.out.println("Simulation Begins....");
		System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Simulation Start ");
		try {
			while (fedAmbassador.getFederateTime() < _simulationEndTime) {
				currentTime = fedAmbassador.getFederateTime();

				if (!eventsList.isEmpty()) {

					nextEventTimestamp = this.getNextEvent().getTime();
					System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Next Message Time: "
							+ nextEventTimestamp);

					/*
					 * Federate must advance its logical time before processing the event. /* A NMR
					 * Request ask RTI to advance time to T. The result is T, if any messages with
					 * timestamp T'<T has to be delivered T' otherwise. In case, appropriate
					 * callbacks are invoked for handling the received event and updating the event
					 * list accordingly
					 */
					fedAmbassador.isAdvancing = true;
					System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Ask a Time Advance");
					rtiAmb.nextMessageRequest(timeFactory.makeTime(nextEventTimestamp));

					// }
					while (fedAmbassador.isAdvancing)
						Thread.sleep(10);
					System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Time Advance Grant");

					event = this.getNextEvent();

					// process event
					process(event);
					eventsList.remove(event);
				} else {
					// no local events. Time is advanced by a fixed step
					nextTime = currentTime + timeStep;
					fedAmbassador.isAdvancing = true;
					rtiAmb.nextMessageRequest(timeFactory.makeTime(nextTime));
					while (fedAmbassador.isAdvancing)
						Thread.sleep(10);
				}

			}
			System.out.println("Simulation Completed");
			System.out.println("___________________________________________" + "\n");
			leaveSimulationExecution();
			displayFederateState();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initFederate(String host) {
		String settings;

		System.out.println("Starting of " + getFederateName());

		try {
			// ---------- create RTIambassador and Connect---------------

			rtiAmb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
			encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

			fedAmbassador = new FederateAmbassadorImpl(this);
			settings = "crcAddress=" + host;
			rtiAmb.connect(fedAmbassador, CallbackModel.HLA_IMMEDIATE, settings);

			System.out.println("Connected to RTI");

			// ---------------- create Federation Execution -------------------

			URL[] fom = new URL[] { (new File("fom/atms_fom.xml")).toURI().toURL(), };// FOM

			rtiAmb.createFederationExecution(FEDERATION_NAME, fom, "HLAinteger64Time");
			System.out.println("Created Federation");

		} catch (FederationExecutionAlreadyExists e) {
			// Exception is ignored
			System.out.println("Connecting to an existing Federation Execution");
		} catch (ErrorReadingFDD | InconsistentFDD | CouldNotCreateLogicalTimeFactory | CouldNotOpenFDD | NotConnected
				| CallNotAllowedFromWithinCallback | RTIinternalError | MalformedURLException e) {
			e.printStackTrace();
		} catch (ConnectionFailed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidLocalSettingsDesignator e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedCallbackModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlreadyConnected e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			// ---------------------- Join Federation -----------------------
			rtiAmb.joinFederationExecution(getFederateName(), FEDERATION_NAME);
			System.out.println("Joined Federation as " + getFederateName());

			// -------------- Sync Points Registering ----------------

			/*
			 * We need to artificially block the federate execution here for showing how
			 * synchronization points RTI call and Federate cal backs work. Specifically a
			 * registered sync point is announced only to federates who actually joined the
			 * federation.
			 */
			FederateHandle f = null;
			if (this.getCode().equals("FCO")) {
				while (f == null) {
					try {
						f = rtiAmb.getFederateHandle("LIN_Airport");
					} catch (FederateNotExecutionMember | NameNotFound ignored) {
					}
				}
			} else {
				while (f == null) {
					try {
						f = rtiAmb.getFederateHandle("FCO_Airport");
					} catch (FederateNotExecutionMember | NameNotFound ignored) {
					}
				}
			}

			// Both federates register and achieve the sync point (see FOM)

			rtiAmb.registerFederationSynchronizationPoint("ReadyToRun", null);

			while (!fedAmbassador.isAnnounced)
				Thread.sleep(10);

			// ---------------------- Time Management -------------------------

			// Federates are both time-regulated and time-constrained
			timeFactory = (HLAinteger64TimeFactory) rtiAmb.getTimeFactory();
			HLAinteger64Interval lookahead = timeFactory.makeInterval(fedAmbassador.federateLookahead);
			rtiAmb.enableTimeRegulation(lookahead);

			while (!fedAmbassador.isRegulating) {
				Thread.sleep(10);
			}
			System.out.println(federateName + " is Time Regulated");

			rtiAmb.enableTimeConstrained();

			while (fedAmbassador.isConstrained == false) {
				Thread.sleep(10);
			}
			System.out.println(federateName + " is Time Constrained");

			// ---------------------- Publish & Subscribe -------------------------

			// Object classes and Attributes
			this.ocOperationalDayHandle = rtiAmb.getObjectClassHandle("HLAobjectRoot.OperationalDay");
			this.flightsScheduledHandle = rtiAmb.getAttributeHandle(ocOperationalDayHandle, "flightsScheduled");
			attributes = rtiAmb.getAttributeHandleSetFactory().create();
			attributes.add(flightsScheduledHandle);

			// Both LIN and FCO publish the operational day scheduled flights
			rtiAmb.publishObjectClassAttributes(ocOperationalDayHandle, attributes);
			rtiAmb.subscribeObjectClassAttributes(ocOperationalDayHandle, attributes);

			// --------------------- Synchronization Before Running -----------------------

			// Sync point reached. The federate must wait other federates
			rtiAmb.synchronizationPointAchieved("ReadyToRun");
			while (!fedAmbassador.isReadyToRun)
				Thread.sleep(10);
			System.out.println(federateName + " All Federates achieved READY_TO_RUN Sync Point");

			// --------------------- Register Object Instance -----------------------
			// Both LIN and FCO register the operational day scheduled flights
			this.instanceODHandle = rtiAmb.registerObjectInstance(ocOperationalDayHandle);

		} catch (FederationExecutionDoesNotExist | SaveInProgress | RestoreInProgress | FederateAlreadyExecutionMember
				| NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError e) {
			System.err.println("Cannot connect to the Federation");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void process(LocalEvent e) {
		System.out.println(
				"[" + fedAmbassador.federateTime + "] " + federateName + ": Processing Event " + e.getEventType());

		Airplane plane;
		long currentTime; // current logical time
		long nextEventTime; // time to schedule the next event
							// (according to the current event in process)
		int timeStep = 10; // timestep for determining the nextEventTime value

		currentTime = fedAmbassador.getFederateTime();

		switch (e.getEventType()) {

		case LANDING_REQUEST:
			plane = ((AirplaneEvent) e).getAirplane();
			// Index of the clear runway
			int runwayClear;
			if ((runwayClear = runwayClear()) != -1) {
				// Tower authorizes Aircraft to land
				// runway is set as busy
				this.setRunwayClearance(runwayClear, false);
				System.out.println("[" + fedAmbassador.federateTime + "] TOWER: Flight" + plane.getFlightCode()
						+ " clear for landing at runway " + runwayClear);
				plane.setState(AirplaneState.LANDED);
				// Once landed, schedule new clear runway event after timeStep time
				addEvent(
						new RunwayEvent(EventType.RUNWAY_CLEARANCE_REQUEST, currentTime + timeStep, runwayClear, true));
				// After LANDING, the flight departure (scheduling of TAKE_OFF_SCHEDULE event)
				// is generated according to the predefined time TIMESTEP
				System.out.println("[" + fedAmbassador.federateTime + "] TOWER: Flight" + plane.getFlightCode()
						+ " prepare for take off at time " + (currentTime + 3 * timeStep));
				addEvent(new AirplaneEvent(EventType.TAKE_OFF_REQUEST, currentTime + 3 * timeStep, plane));

			} else {
				// If runways are busy, a new LANDING_REQUEST is generated at current time + time
				// step minutes
				System.out.println("[" + fedAmbassador.federateTime + "] TOWER: Flight" + plane.getFlightCode()
						+ " NOT clear for landing ");
				nextEventTime = currentTime + timeStep;
				System.out.println("[" + fedAmbassador.federateTime + "] TOWER: Flight" + plane.getFlightCode()
						+ " fly around and ask again at time " + nextEventTime);
				addEvent(new AirplaneEvent(EventType.LANDING_REQUEST, nextEventTime, plane));
			}

			break;
		case TAKE_OFF_REQUEST:

			plane = ((AirplaneEvent) e).getAirplane();

			if ((runwayClear = runwayClear()) != -1) {
				// Set busy this runway
				this.setRunwayClearance(runwayClear, false);
				System.out.println(
						"[" + fedAmbassador.federateTime + "] TOWER: Runway " + runwayClear + " now is busy. ");
				// TAKE_OFF_REQUEST generates an update to the operationalDay Object Class.

				// Switch source and destination airport of the plane in return flight
				if (plane.getDestinationAirport().equals(this.getCode())) {
					String sourceAirport = plane.getAirport();
					plane.setAirport(plane.getDestinationAirport());
					plane.setDestinationAirport(sourceAirport);
				}

				// The airplane disappear from the tower radar
				// (e.g., it is removed from the list of managed planes).
				// A remote event "LANDING_REQUEST" is scheduled (e.g. a new record of
				// operational day is added).
				// Its arrival time at the remote airport is current time + travelTime of the
				// airplane
				nextEventTime = currentTime + plane.getTravelTime();
				System.out.println("[" + fedAmbassador.federateTime + "] TOWER: Flight" + plane.getFlightCode()
						+ " clear for take off from runway " + runwayClear);
				System.out.println("Arrival to " + plane.getDestinationAirport() + " at time " + nextEventTime);
				// Set runway clear after timeStep time
				addEvent(
						new RunwayEvent(EventType.RUNWAY_CLEARANCE_REQUEST, currentTime + timeStep, runwayClear, true));
				// airplane is removed from the list of planes under the control of the Tower
				this.managedAirplanes.remove(plane);
				// Schedule departure from this airport
				this.scheduleNewFlight(nextEventTime, plane);
			} else {
				// If runways are busy, a new TAKE_OFF_REQUEST is generated at current time + time
				// step minutes
				System.out.println("[" + fedAmbassador.federateTime + "] TOWER: Flight" + plane.getFlightCode()
						+ " NOT clear for departure ");
				nextEventTime = currentTime + timeStep;
				System.out.println("[" + fedAmbassador.federateTime + "] TOWER: Flight" + plane.getFlightCode()
						+ " wait and ask again at time " + nextEventTime);
				addEvent(new AirplaneEvent(EventType.TAKE_OFF_REQUEST, nextEventTime, plane));
			}
			break;
		case RUNWAY_CLEARANCE_REQUEST:
			RunwayEvent re = (RunwayEvent) e;
			int runway = re.getRunway();
			boolean status = re.isClearance();
			this.setRunwayClearance(runway, status);
			System.out.println("[" + fedAmbassador.federateTime + "] TOWER: Runway " + runway + " now is "
					+ (status ? "clear" : "busy"));
			break;
		}

	}

	private void notifyOperationalDayInsert(long nextEventTime, Airplane a) {
		// HLAfixedRecord element used for encoding the flightScheduled attribute
		// which includes timeScheduled and a HLAfixedRecord airplaneRecord
		HLAfixedRecord flightsScheduledEncoder = encoderFactory.createHLAfixedRecord();
		HLAfixedRecord airplaneRecordEncoder = encoderFactory.createHLAfixedRecord();
		HLAinteger64BE timeScheduledEncoder = encoderFactory.createHLAinteger64BE();
		timeScheduledEncoder.setValue(nextEventTime);

		HLAunicodeString flightCodeEncoder = encoderFactory.createHLAunicodeString();
		flightCodeEncoder.setValue(a.getFlightCode());
		HLAunicodeString airportEncoder = encoderFactory.createHLAunicodeString();
		airportEncoder.setValue(a.getAirport());
		HLAunicodeString destAirportEncoder = encoderFactory.createHLAunicodeString();
		destAirportEncoder.setValue(a.getDestinationAirport());
		HLAinteger64BE travelTimeEncoder = encoderFactory.createHLAinteger64BE();
		travelTimeEncoder.setValue(a.getTravelTime());

		airplaneRecordEncoder.add(flightCodeEncoder);
		airplaneRecordEncoder.add(airportEncoder);
		airplaneRecordEncoder.add(destAirportEncoder);
		airplaneRecordEncoder.add(travelTimeEncoder);

		flightsScheduledEncoder.add(timeScheduledEncoder);
		flightsScheduledEncoder.add(airplaneRecordEncoder);

		try {
			// hashmap of size = 1 (the object class has only 1 attribute)
			AttributeHandleValueMap attributeValues = rtiAmb.getAttributeHandleValueMapFactory().create(1);
			// put an element into the map
			// - objectclass handle is the key, the content is a byte stream generated from
			// the attribute (HLAfixedRecord element)
			attributeValues.put(flightsScheduledHandle, flightsScheduledEncoder.toByteArray());
			HLAinteger64Time time = timeFactory.makeTime(nextEventTime);
			// Acquire ownership of flights scheduled of the OperationalDay Object Class
			if (!rtiAmb.isAttributeOwnedByFederate(instanceODHandle, flightsScheduledHandle)) {
				this.fedAmbassador.pendingAcquisition = true;
				rtiAmb.attributeOwnershipAcquisition(instanceODHandle, attributes, null);
				while (fedAmbassador.pendingAcquisition)
					Thread.sleep(10);
			}

			// update attribute
			rtiAmb.updateAttributeValues(instanceODHandle, attributeValues,
					OperationalDay.UpdateType.INSERT.name().getBytes(), time);

		} catch (ObjectInstanceNotKnown e) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void leaveSimulationExecution() {

		// ---------- Simulation Main Loop ---------------

		try {
			rtiAmb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
		} catch (FederateOwnsAttributes ignored) {
		}

		catch (InvalidResignAction | OwnershipAcquisitionPending | FederateNotExecutionMember | NotConnected
				| CallNotAllowedFromWithinCallback | RTIinternalError e) {

			e.printStackTrace();
		}
		System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Resigned from Federation");

		try {
			rtiAmb.destroyFederationExecution(this.FEDERATION_NAME);
			System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": destroyed federation");
		} catch (FederatesCurrentlyJoined ej) {
			// ignored
			System.out.println("[" + fedAmbassador.federateTime + "] " + federateName
					+ ": did not destroy federation, federates still joined");
		} catch (FederationExecutionDoesNotExist e2) {
			// ignored
			System.out.println("[" + fedAmbassador.federateTime + "] " + federateName
					+ ": tried to destroy federation, but federation is just destroyed");
		} catch (NotConnected | RTIinternalError e) {
			e.printStackTrace();
		}
	}

	private void displayFederateState() {
		// ---------- 10 Simulation Main Loop ---------------
		System.out.println("\n" + federateName + " Final State Summary");
		System.out.println("Runway Clearance State: " + Arrays.toString(this.isRunwayClear));

		System.out.println("Managed Airplanes:");
		managedAirplanes.forEach((a) -> System.out.println(a.getFlightCode() + " " + a.getState()));

		System.out.println("Events Queue: ");
		Iterator<LocalEvent> i = this.eventsList.iterator();
		LocalEvent le;
		while (i.hasNext()) {
			le = (LocalEvent) i.next();
			System.out.println(le.getEventType() + " expected at time " + le.getTime());
		}

		System.out.println("Flights remained: ");
		SortedMap<Long, Airplane> flightsRemained = this.operationalDay.getFlightsScheduled()
				.tailMap(this._simulationEndTime + 1);
		flightsRemained.forEach((time, airplane) -> {
			System.out.println("<" + time + "," + airplane.getFlightCode() + ">");
		});

	}
}
