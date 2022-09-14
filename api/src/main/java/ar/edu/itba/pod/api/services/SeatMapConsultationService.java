package ar.edu.itba.pod.api.services;

import ar.edu.itba.pod.api.entities.Seat;
import ar.edu.itba.pod.api.entities.SeatCategory;
import ar.edu.itba.pod.api.exceptions.FlightDoesntExistException;
import ar.edu.itba.pod.api.exceptions.SeatCategoryDoesntExistException;
import ar.edu.itba.pod.api.exceptions.SeatRowDoesntExistException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface SeatMapConsultationService extends Remote {

    List<Seat> consultSeatMap(String flightCode) throws RemoteException, FlightDoesntExistException;
    List<Seat> consultSeatMap(String flightCode, SeatCategory category) throws RemoteException, FlightDoesntExistException, SeatCategoryDoesntExistException;
    List<Seat> consultSeatMap(String flightCode, int row) throws RemoteException, FlightDoesntExistException, SeatRowDoesntExistException;

}
