package ar.edu.itba.pod.api.exceptions;

public class FlightIsNotPendingException extends Exception{
    public FlightIsNotPendingException(String flightCode){
        super(String.format("The flight with '%s' code is not pending!\n",flightCode));
    }
}
