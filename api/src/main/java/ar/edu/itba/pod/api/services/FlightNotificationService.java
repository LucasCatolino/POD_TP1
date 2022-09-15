package ar.edu.itba.pod.api.services;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ar.edu.itba.pod.api.exceptions.FlightDoesntExistException;
import ar.edu.itba.pod.api.exceptions.PassengerNotInFlightException;
import ar.edu.itba.pod.api.exceptions.PassengerNotSubscribedException;
import ar.edu.itba.pod.api.utils.Pair;

public interface FlightNotificationService extends Remote {
    void registerPassengerToNotify(String flightCode,String passengerName) throws RemoteException, FlightDoesntExistException, PassengerNotInFlightException, PassengerNotSubscribedException;
    Pair<Integer, String> notify(String flightcode, String passengerName) throws RemoteException, PassengerNotSubscribedException;
}
