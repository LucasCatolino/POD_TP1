package ar.edu.itba.pod.api.services;

import ar.edu.itba.pod.api.entities.Seat;
import ar.edu.itba.pod.api.entities.SeatCategory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface SeatMapConsultationService extends Remote {

    List<Seat> consultSeatMap(String flightCode) throws RemoteException;
    List<Seat> consultSeatMap(String flightCode, SeatCategory category) throws RemoteException;
    List<Seat> consultSeatMap(String flightCode, int row) throws RemoteException;

}
