package ar.edu.itba.pod.api.exceptions;

public class PlaneModelAlreadyExistsException extends Exception{
    public PlaneModelAlreadyExistsException(String s) { super(String.format("Plane model %s already exists!\n",s));}

}
