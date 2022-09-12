package ar.edu.itba.pod.api.services;


import ar.edu.itba.pod.api.entities.FlightStatus;
import ar.edu.itba.pod.api.entities.SeatCategory;
import ar.edu.itba.pod.api.entities.Ticket;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.SortedSet;
import java.util.Map;

public interface FlightManagementService extends Remote {

    void addPlaneModel(String modelName,
                        int businessRows,int businessCols,
                        int epRows, int epCols,
                        int econRows, int econCols) throws RemoteException;
                        
    void addFlight(String planeModelName, String flightCode, String destinyAirportCode, SortedSet<Ticket> tickets) throws RemoteException;

    FlightStatus checkFlightStatus(String flightCode) throws RemoteException;

    void confirmFlight(String flightCode) throws RemoteException;

    void cancelFlight(String flightCode) throws RemoteException;

    void forceTicketChange() throws RemoteException;
}
