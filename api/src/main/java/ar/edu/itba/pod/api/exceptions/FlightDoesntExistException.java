package ar.edu.itba.pod.api.exceptions;

public class FlightDoesntExistException extends Exception{
    public FlightDoesntExistException(String s) { super(String.format("The flight with '%s' code doesn't exists!\n",s));}

}
