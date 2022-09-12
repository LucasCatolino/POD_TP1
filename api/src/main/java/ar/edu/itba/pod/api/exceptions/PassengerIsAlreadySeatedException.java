package ar.edu.itba.pod.api.exceptions;

public class PassengerIsAlreadySeatedException extends Exception{
    public PassengerIsAlreadySeatedException(String passName, String code){
        super(String.format("Passenger '%s' already has a seat in flight '%s'!\n", passName, code));
    }
}
