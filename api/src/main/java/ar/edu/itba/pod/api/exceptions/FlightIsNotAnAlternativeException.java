package ar.edu.itba.pod.api.exceptions;

public class FlightIsNotAnAlternativeException extends Exception{
    public FlightIsNotAnAlternativeException(String flightcode, String passenger) { 
        super(String.format("The flight with '%s' code is not an alternative flight for '$s'!\n", flightcode, passenger));
    }
}