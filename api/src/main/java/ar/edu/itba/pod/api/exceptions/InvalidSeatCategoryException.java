package ar.edu.itba.pod.api.exceptions;

public class InvalidSeatCategoryException extends Exception{
    public InvalidSeatCategoryException(){
        super(String.format("Seat category is not valid!\n"));
    }
}