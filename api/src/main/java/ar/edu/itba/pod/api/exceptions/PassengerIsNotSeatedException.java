package ar.edu.itba.pod.api.exceptions;

public class PassengerIsNotSeatedException extends Exception{
    public PassengerIsNotSeatedException(String passengerName, String flightCode){
        super(String.format("Passenger '%s' is not seated at flight '%s'!\n", passengerName, flightCode));

    }

}