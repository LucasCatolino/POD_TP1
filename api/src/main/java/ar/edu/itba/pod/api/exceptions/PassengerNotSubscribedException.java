package ar.edu.itba.pod.api.exceptions;

public class PassengerNotSubscribedException extends Exception{
    public PassengerNotSubscribedException(String n, String fc){
        super(String.format("The passenger '%s' is not subscribed to '%s' notifications\n", n, fc));
    }
}