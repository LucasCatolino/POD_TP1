package ar.edu.itba.pod.api.services;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightNotificationService extends Remote {
    void registerPassengerToNotify(String flightCode,String passengerName) throws RemoteException;

}
