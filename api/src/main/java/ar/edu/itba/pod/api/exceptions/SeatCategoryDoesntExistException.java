package ar.edu.itba.pod.api.exceptions;
import ar.edu.itba.pod.api.entities.*;

public class SeatCategoryDoesntExistException extends Exception{
    public SeatCategoryDoesntExistException(String flightCode, String cat){
        super(String.format("Seat category '%s' for flight '%s' doesn't exist\n", cat,flightCode));
    }
}
