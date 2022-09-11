package ar.edu.itba.pod.api.exceptions;

public class TicketNotInFlightException extends Exception {
    public TicketNotInFlightException(String flightNumber, String passengerName) { super(String.format("Passenger " + passengerName + " is not in flight " + flightNumber + "!\n"));}
}