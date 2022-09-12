package ar.edu.itba.pod.api.exceptions;

public class PassengerDoesntHaveTicketException extends Exception{
    public PassengerDoesntHaveTicketException(String passName, String code){
        super(String.format("Passenger '%s' does not have a ticket in flight '%s'!\n", passName, code));
    }
}
