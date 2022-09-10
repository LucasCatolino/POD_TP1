package ar.edu.itba.pod.api.services;

import ar.edu.itba.pod.api.entities.Seat;
import ar.edu.itba.pod.api.entities.SeatCategory;

import java.rmi.Remote;
import java.util.List;


public interface SeatMapConsultationService extends Remote {

    List<Seat> consultSeatMap(String flightCode);
    List<Seat> consultSeatMap(String flightCode, SeatCategory category);
    List<Seat> consultSeatMap(String flightCode, int row);

}
