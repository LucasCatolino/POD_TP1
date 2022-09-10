package ar.edu.itba.pod.api.entities;

import java.util.List;

public class Flight {
    PlaneModel model;

    List<Seat> seats;

    List<Ticket> passengers;

    FlightStatus status;

    String flightCode;
    String destinyAirportCode;

}
