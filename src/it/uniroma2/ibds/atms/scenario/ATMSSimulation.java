package it.uniroma2.ibds.atms.scenario;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

import it.uniroma2.ibds.atms.events.AirplaneEvent;
import it.uniroma2.ibds.atms.events.EventType;
import it.uniroma2.ibds.atms.events.LocalEvent;
import it.uniroma2.ibds.atms.federate.Airport;
import it.uniroma2.ibds.atms.federate.OperationalDay;

public class ATMSSimulation {

	// Scenario
	private Airport airportFederate;
	private int day = 0;
	private static int _seed = 67;
	// We consider simulation time as minutes, so, in order to simulate from 5:00 AM
	// to 11:00 PM, we set simulation end time as 18*60=1080
	private static long _simulationEndTIme = 1080;

	// main simulation method
	void run(String[] args) {
		String airportCode = "FCO";
		String code;
		String host = "localhost";
		// Both airport have 2 runways
		final int n_runways = 2;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		// Instantiate a sample calendar with 24 and 25 October 2022
		OperationalDay[] calendar = { new OperationalDay(new Date(24, 10, 2022)),
				new OperationalDay(new Date(25, 10, 2022)) };

		// instatiating federate
		try {
			System.out.println("Air Traffic Control Simulation");
			System.out.println("_______________________________" + "\n");
			System.out.println("Enter the Airport Code");
			System.out.println("1: FCO - Fiumicino (default)");
			System.out.println("2: LIN - Linate");

			code = in.readLine();
			if (code.equals("2"))
				airportCode = "LIN";

			System.out.println("Enter CRC Host or press enter for localhost");
			host = in.readLine();
			if (host.length() == 0)
				host = "localhost";

			// User choose which day want to consider in the simulation
			System.out.println("Enter the operational day you want simulate");
			System.out.println("0: 24/10/2022");
			System.out.println("1: 25/10/2022");
			day = Integer.parseInt(in.readLine());
			if(day >= calendar.length)
				throw new IllegalArgumentException("Day not implemented yet");
		} catch (Exception e) {
			e.printStackTrace();
		}

		airportFederate = new Airport(airportCode, n_runways, calendar[day], _seed, _simulationEndTIme);

		// Scenario Configuration
		initScenario(airportCode);

		// Starting of Federate Execution
		airportFederate.startFederate(host);
	}

	private void initScenario(String airportCode) {
		// Set all runways as clear
		for (int i = 0; i < airportFederate.getNumberOfRunways(); i++)
			airportFederate.setRunwayClearance(i, true);
		switch (day) {
		case 0:
			if (airportCode == "FCO") {
				// Fiumicino Airport, busy runways, 2 managed planes, 3 initial events
				Airplane a1 = new Airplane(AirplaneState.LANDED, "AZ001", "FCO", "LIN", 30);
				Airplane a2 = new Airplane(AirplaneState.LANDED, "AZ002", "FCO", "NAP", 30);
				Airplane a5 = new Airplane(AirplaneState.IN_FLIGHT, "AZ005", "LIN", "FCO", 30);
				Airplane a3 = new Airplane(AirplaneState.IN_FLIGHT, "AZ003", "LIN", "FCO", 30);

				airportFederate.addManagedAirplane(a1);
				airportFederate.addManagedAirplane(a2);

				airportFederate.addEvent(new AirplaneEvent(EventType.TAKE_OFF_REQUEST, (long) 60, a1));
				airportFederate.addEvent(new AirplaneEvent(EventType.TAKE_OFF_REQUEST, (long) 60, a2));
				airportFederate.addEvent(new AirplaneEvent(EventType.LANDING_REQUEST, (long) 60, a5));
				airportFederate.addEvent(new AirplaneEvent(EventType.LANDING_REQUEST, (long) 75, a3));

			} else if (airportCode == "LIN") {
				// LINATE Airport, clear runways, 1 managed plane, 1 initial event
				Airplane a4 = new Airplane(AirplaneState.LANDED, "AZ004", "LIN", "FCO", 30);

				airportFederate.addManagedAirplane(a4);

				airportFederate.addEvent(new AirplaneEvent(EventType.TAKE_OFF_REQUEST, (long) 600, a4));

			} else {
				throw new IllegalArgumentException("Airport Code Unknown");
			}
			break;
		case 1:
			if (airportCode == "FCO") {
				// Fiumicino Airport, busy runways, 2 managed planes, 1 initial event
				Airplane a3 = new Airplane(AirplaneState.IN_FLIGHT, "AZ003", "LIN", "FCO", 30);
				Airplane a4 = new Airplane(AirplaneState.LANDED, "AZ004", "FCO", "LIN", 30);

				airportFederate.addManagedAirplane(a3);
				airportFederate.addManagedAirplane(a4);

				airportFederate.addEvent(new AirplaneEvent(EventType.LANDING_REQUEST, (long) 720, a3));

			} else if (airportCode == "LIN") {
				// LINATE Airport, clear runways, 1 managed plane, 1 initial event
				Airplane a1 = new Airplane(AirplaneState.LANDED, "AZ001", "LIN", "FCO", 30);
				Airplane a2 = new Airplane(AirplaneState.LANDED, "AZ002", "LIN", "NAP", 30);
				Airplane a5 = new Airplane(AirplaneState.LANDED, "AZ005", "LIN", "FCO", 30);
				Airplane a6 = new Airplane(AirplaneState.LANDED, "AZ006", "LIN", "FCO", 30);
				Airplane a7 = new Airplane(AirplaneState.IN_FLIGHT, "AZ007", "FCO", "LIN", 30);

				airportFederate.addManagedAirplane(a1);
				airportFederate.addManagedAirplane(a2);
				airportFederate.addManagedAirplane(a5);
				airportFederate.addManagedAirplane(a6);
				airportFederate.addManagedAirplane(a7);

				airportFederate.addEvent(new AirplaneEvent(EventType.TAKE_OFF_REQUEST, (long) 720, a1));
				airportFederate.addEvent(new AirplaneEvent(EventType.TAKE_OFF_REQUEST, (long) 720, a2));
				airportFederate.addEvent(new AirplaneEvent(EventType.TAKE_OFF_REQUEST, (long) 725, a5));
				airportFederate.addEvent(new AirplaneEvent(EventType.TAKE_OFF_REQUEST, (long) 725, a6));
				airportFederate.addEvent(new AirplaneEvent(EventType.LANDING_REQUEST, (long) 735, a7));

			} else {
				System.out.println("Airport Code Unknown");
			}
			break;
		}
		System.out.println("Completed Scenario Configuration for Airport " + airportCode);
	}

	public static void main(String[] args) {
		new ATMSSimulation().run(args);

	}

}
