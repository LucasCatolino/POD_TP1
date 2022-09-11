package ar.edu.itba.pod.api.exceptions;

public class FlightAlreadyExistsException extends Exception {
    public FlightAlreadyExistsException(String s) { super(String.format("The flight with '%s' code already exists!\n",s));}
}
