package ar.edu.itba.pod.api.exceptions;

public class PlaneModelDoesntExistsException extends Exception {
    public PlaneModelDoesntExistsException(String s) { super(String.format("Plane model %s doesn't exists!\n",s));}
}
