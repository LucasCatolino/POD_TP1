package ar.edu.itba.pod.server;

import ar.edu.itba.pod.api.entities.FlightStatus;
import ar.edu.itba.pod.api.entities.Seat;
import ar.edu.itba.pod.api.entities.SeatCategory;
import ar.edu.itba.pod.api.entities.Ticket;
import ar.edu.itba.pod.api.exceptions.*;
import ar.edu.itba.pod.api.services.FlightManagementService;
import ar.edu.itba.pod.api.services.FlightNotificationService;
import ar.edu.itba.pod.api.services.SeatAssignmentService;
import ar.edu.itba.pod.api.services.SeatMapConsultationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public class Servant implements SeatMapConsultationService, FlightNotificationService, SeatAssignmentService, FlightManagementService {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final FlightsManagement fm = new FlightsManagement();

    @Override
    public void addPlaneModel(String modelName, int businessRows, int businessCols, int epRows, int epCols, int econRows, int econCols) throws RemoteException {
        try {
            fm.createNewPlaneModel(modelName, businessRows, businessCols, epRows, epCols, econRows, econCols);
        } catch (PlaneModelAlreadyExistsException e) {
            throw new RuntimeException(e);
        } 
    }

    @Override
    public void addFlight(String planeModelName, String flightCode, String destinyAirportCode, SortedSet<Ticket> tickets) throws RemoteException {
        try {
            fm.createNewFlight(planeModelName, flightCode, destinyAirportCode, tickets);
        } catch (PlaneModelDoesntExistsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FlightStatus checkFlightStatus(String flightCode) throws RemoteException {
        try {
            return fm.checkFlightStatus(flightCode);
        } catch (FlightDoesntExistException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void confirmFlight(String flightCode) throws RemoteException {
        try {
            fm.confirmFlight(flightCode);
        } catch (FlightDoesntExistException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cancelFlight(String flightCode) throws RemoteException {
        try {
            fm.cancelFlight(flightCode);
        } catch (FlightDoesntExistException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void forceTicketChange()  throws RemoteException {
        try {
            fm.forceTicketChange();
        } catch (TicketNotInFlightException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerPassengerToNotify(String flightCode, String passengerName) throws RemoteException {

    }

    @Override
    public boolean seatIsOccupied(String flightCode, int row, char column)  throws RemoteException{
        try {
            return fm.seatIsOccupied(flightCode, row, column);
        } catch (FlightDoesntExistException e) {
            throw new RuntimeException(e);
        } catch (SeatDoesntExistException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assignNewSeatToPassenger(String flightCode, String passengerName, int row, char column)  throws RemoteException{
        try {
            fm.assignNewSeatToPassenger(flightCode, passengerName, row, column);
        } catch (FlightDoesntExistException | FlightIsNotPendingException | PassengerDoesntHaveTicketException
                | PassengerIsAlreadySeatedException | SeatIsTakenException | InvalidSeatCategoryException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void movePassengerToNewSeat(String flightCode, String passengerName, int row, char column)  throws RemoteException{
        try {
            fm.resitPassenger(flightCode, passengerName, row, column);
        } catch (FlightDoesntExistException | FlightIsNotPendingException | PassengerDoesntHaveTicketException
                | PassengerIsAlreadySeatedException | SeatIsTakenException | InvalidSeatCategoryException
                | PassengerIsNotSeatedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<Seat>> listAlternativeFlightSeats(String flightCode, String passengerName) throws RemoteException {
        try {
            return fm.listAlternativeFlightSeats(flightCode, passengerName);
        } catch (FlightDoesntExistException e){
            throw new RuntimeException(e);
        } catch (PassengerDoesntHaveTicketException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changePassengerFlight(String passengerName, String oldFlightCode, String newFlightCode)  throws RemoteException{
        try {
            fm.changePassengerFlight(passengerName, oldFlightCode, newFlightCode);
        } catch (FlightDoesntExistException | FlightIsNotPendingException | TicketNotInFlightException
                | FlightIsNotAnAlternativeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Seat> consultSeatMap(String flightCode)  throws RemoteException{
        try {
            return fm.consultSeatMap(flightCode);
        } catch (FlightDoesntExistException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Seat> consultSeatMap(String flightCode, SeatCategory category)  throws RemoteException{
        try {
            return fm.consultSeatMap(flightCode, category);
        } catch (FlightDoesntExistException | SeatCategoryDoesntExistException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Seat> consultSeatMap(String flightCode, int row)  throws RemoteException{
        try {
            return fm.consultSeatMap(flightCode, row);
        } catch (FlightDoesntExistException | SeatRowDoesntExistException e) {
            throw new RuntimeException(e);
        }
    }
}
