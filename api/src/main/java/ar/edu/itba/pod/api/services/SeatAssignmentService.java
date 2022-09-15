package ar.edu.itba.pod.api.services;

import ar.edu.itba.pod.api.entities.*;
import ar.edu.itba.pod.api.exceptions.*;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface SeatAssignmentService extends Remote {

    String seatIsOccupied(String flightCode, int row, char column) throws RemoteException, FlightDoesntExistException, SeatDoesntExistException;

    void assignNewSeatToPassenger(String flightCode, String passengerName, int row, char column) throws RemoteException, FlightDoesntExistException, FlightIsNotPendingException, PassengerDoesntHaveTicketException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException, SeatDoesntExistException;

    // Este método cambia al pasajero de asiento en un mismo vuelo.
    void movePassengerToNewSeat(String flightCode, String passengerName, int row, char column) throws RemoteException, FlightDoesntExistException, FlightIsNotPendingException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException, PassengerIsNotSeatedException, PassengerDoesntHaveTicketException, SeatDoesntExistException;

    //Devuelve lista de Pairs con <FlightCode, Lista de asientos disponibles>
    List<Flight> listAlternativeFlightSeats(String flightCode, String passengerName) throws RemoteException, FlightDoesntExistException, PassengerDoesntHaveTicketException;

    //Este método cambia el pasajero de vuelo a otro avión.
    void changePassengerFlight(String passengerName, String oldFlightCode, String newFlightCode) throws RemoteException, FlightDoesntExistException, FlightIsNotPendingException, TicketNotInFlightException, FlightIsNotAnAlternativeException, PassengerDoesntHaveTicketException, PassengerNotInFlightException;

}
