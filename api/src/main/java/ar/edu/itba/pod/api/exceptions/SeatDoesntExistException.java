package ar.edu.itba.pod.api.exceptions;

public class SeatDoesntExistException extends Exception{
    public SeatDoesntExistException(int row, char column) { super(String.format("Seat in row " + row + " column " + column + " does not exist!\n"));}
}
