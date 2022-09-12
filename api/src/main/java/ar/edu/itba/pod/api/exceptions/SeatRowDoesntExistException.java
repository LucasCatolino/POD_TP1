package ar.edu.itba.pod.api.exceptions;

public class SeatRowDoesntExistException extends Exception{
    public SeatRowDoesntExistException(String flightCode, int row){
        super(String.format("Seat row '%d' for flight '%s' doesn't exist\n", row, flightCode));
    }
}
