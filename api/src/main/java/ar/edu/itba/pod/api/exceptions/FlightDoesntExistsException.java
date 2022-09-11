package ar.edu.itba.pod.api.exceptions;

public class FlightDoesntExistsException extends Exception{
    public FlightDoesntExistsException(String s) { super(String.format("The flight with '%s' code doesn't exists!\n",s));}

}
