package ar.edu.itba.pod.api.services;

import java.rmi.Remote;

public interface FlightNotificationService extends Remote {
    void registerPassengerToNotify(String flightCode,String passengerName);

}
