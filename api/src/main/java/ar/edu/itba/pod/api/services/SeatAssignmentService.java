package ar.edu.itba.pod.api.services;

import ar.edu.itba.pod.api.entities.Seat;
import org.apache.commons.lang3.tuple.Pair;

import java.rmi.Remote;
import java.util.List;

public interface SeatAssignmentService extends Remote {

    boolean seatIsOccupied(String flightCode, int row, char column);

    void assignNewSeatToPassenger(String flightCode, String passengerName, int row, char column);

    // Este método cambia al pasajero de asiento en un mismo vuelo.
    void movePassengerToNewSeat(String flightCode, String passengerName, int row, char column);

    //Devuelve lista de Pairs con <FlightCode, Lista de asientos disponibles>
    List<Pair<String,List<Seat>>> listAlternativeFlightSeats(String flightCode, String passengerName);

    //Este método cambia el pasajero de vuelo a otro avión.
    void changePassengerFlight(String passengerName, String oldFlightCode, String newFlightCode);

}
