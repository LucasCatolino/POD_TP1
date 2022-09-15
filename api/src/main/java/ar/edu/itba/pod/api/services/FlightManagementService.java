package ar.edu.itba.pod.api.services;

import ar.edu.itba.pod.api.utils.Pair;
import ar.edu.itba.pod.api.entities.FlightStatus;
import ar.edu.itba.pod.api.entities.Ticket;
import ar.edu.itba.pod.api.exceptions.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.SortedSet;
import java.util.Map;
import java.util.List;

public interface FlightManagementService extends Remote {

    void addPlaneModel(String modelName, int businessRows,int businessCols, int epRows, int epCols, int econRows, int econCols) throws RemoteException, PlaneModelAlreadyExistsException;

    void addFlight(String planeModelName, String flightCode, String destinyAirportCode, SortedSet<Ticket> tickets) throws RemoteException, PlaneModelDoesntExistsException, FlightAlreadyExistsException;

    FlightStatus checkFlightStatus(String flightCode) throws RemoteException, FlightDoesntExistException;

    void confirmFlight(String flightCode) throws RemoteException, FlightDoesntExistException;

    void cancelFlight(String flightCode) throws RemoteException, FlightDoesntExistException;

    Pair<Integer,Map<String,List<String>>> forceTicketChange() throws RemoteException, TicketNotInFlightException, FlightDoesntExistException, PassengerNotSubscribedException, PassengerNotInFlightException;
}
