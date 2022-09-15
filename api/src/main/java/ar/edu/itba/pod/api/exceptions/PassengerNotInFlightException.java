package ar.edu.itba.pod.api.exceptions;

public class PassengerNotInFlightException extends Exception {
    public PassengerNotInFlightException(String s) { super(String.format("The passenger '%s' is not on this flight! :(\n",s));}

}