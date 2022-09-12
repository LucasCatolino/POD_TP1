package ar.edu.itba.pod.api.exceptions;

public class SeatIsTakenException extends Exception{
    public SeatIsTakenException(String flightCode, int row, char column){
        super(String.format("Seat '%d''%c' is already taken in flight '%s'\n", row, column, flightCode));
    }
}
