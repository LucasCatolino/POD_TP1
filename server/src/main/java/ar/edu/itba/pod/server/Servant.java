package ar.edu.itba.pod.server;

import ar.edu.itba.pod.api.entities.FlightStatus;
import ar.edu.itba.pod.api.entities.Seat;
import ar.edu.itba.pod.api.entities.SeatCategory;
import ar.edu.itba.pod.api.entities.Ticket;
import ar.edu.itba.pod.api.exceptions.FlightDoesntExistsException;
import ar.edu.itba.pod.api.exceptions.PlaneModelDoesntExistsException;
import ar.edu.itba.pod.api.services.FlightManagementService;
import ar.edu.itba.pod.api.services.FlightNotificationService;
import ar.edu.itba.pod.api.services.SeatAssignmentService;
import ar.edu.itba.pod.api.services.SeatMapConsultationService;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.List;

public class Servant implements SeatMapConsultationService, FlightNotificationService, SeatAssignmentService, FlightManagementService {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final FlightsManagement fm = new FlightsManagement();

    @Override
    public void addPlaneModel(String modelName, int businessRows, int businessCols, int epRows, int epCols, int econRows, int econCols) throws RemoteException {
        try {
            fm.createNewPlaneModel(modelName, businessRows, businessCols, epRows, epCols, econRows, econCols);
        } catch (PlaneModelDoesntExistsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFlight(String planeModelName, String flightCode, String destinyAirportCode, List<Ticket> tickets) throws RemoteException {
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
        } catch (FlightDoesntExistsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void confirmFlight(String flightCode) throws RemoteException {
        try {
            fm.confirmFlight(flightCode);
        } catch (FlightDoesntExistsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cancelFlight(String flightCode) throws RemoteException {
        try {
            fm.cancelFlight(flightCode);
        } catch (FlightDoesntExistsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void forceTicketChange()  throws RemoteException{

    }

    @Override
    public void registerPassengerToNotify(String flightCode, String passengerName) throws RemoteException {

    }

    @Override
    public boolean seatIsOccupied(String flightCode, int row, char column)  throws RemoteException{
        return false;
    }

    @Override
    public void assignNewSeatToPassenger(String flightCode, String passengerName, int row, char column)  throws RemoteException{

    }

    @Override
    public void movePassengerToNewSeat(String flightCode, String passengerName, int row, char column)  throws RemoteException{

    }

    @Override
    public List<Pair<String, List<Seat>>> listAlternativeFlightSeats(String flightCode, String passengerName) throws RemoteException {
        return null;
    }

    @Override
    public void changePassengerFlight(String passengerName, String oldFlightCode, String newFlightCode)  throws RemoteException{

    }

    @Override
    public List<Seat> consultSeatMap(String flightCode)  throws RemoteException{
        return null;
    }

    @Override
    public List<Seat> consultSeatMap(String flightCode, SeatCategory category)  throws RemoteException{
        return null;
    }

    @Override
    public List<Seat> consultSeatMap(String flightCode, int row)  throws RemoteException{
        return null;
    }
}
