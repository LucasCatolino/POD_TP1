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
import ar.edu.itba.pod.api.utils.Pair;
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
    public void addPlaneModel(String modelName, int businessRows, int businessCols, int epRows, int epCols, int econRows, int econCols) throws RemoteException, PlaneModelAlreadyExistsException {
        fm.createNewPlaneModel(modelName, businessRows, businessCols, epRows, epCols, econRows, econCols);
        logger.info("model {} added", modelName);
    }

    @Override
    public void addFlight(String planeModelName, String flightCode, String destinyAirportCode, SortedSet<Ticket> tickets) throws RemoteException, PlaneModelDoesntExistsException, FlightAlreadyExistsException {
        fm.createNewFlight(planeModelName, flightCode, destinyAirportCode, tickets);
        logger.info("Flight {} added", flightCode);
    }

    @Override
    public FlightStatus checkFlightStatus(String flightCode) throws RemoteException, FlightDoesntExistException {
        return fm.checkFlightStatus(flightCode);
       // logger.info("Checked status for flight {}", flightCode);
    }

    @Override
    public void confirmFlight(String flightCode) throws RemoteException, FlightDoesntExistException {
        fm.confirmFlight(flightCode);
        logger.info("Confirmed flight {}", flightCode);
    }

    @Override
    public void cancelFlight(String flightCode) throws RemoteException, FlightDoesntExistException {
        fm.cancelFlight(flightCode);
        logger.info("Flight {} cancelled",flightCode);
    }

    @Override
    public Pair<Integer, Map<String, List<String>>> forceTicketChange()  throws RemoteException, TicketNotInFlightException {
        Pair<Integer,Map<String,List<String>>> ret =  fm.forceTicketChange();
        logger.info("Forced ticket change");
        return ret;
    }

    @Override
    public void registerPassengerToNotify(String flightCode, String passengerName) throws RemoteException {
        //TODO:
    }

    @Override
    public String seatIsOccupied(String flightCode, int row, char column)  throws RemoteException, FlightDoesntExistException, SeatDoesntExistException {

        String ret = fm.seatIsOccupied(flightCode, row, column);
        if(ret.equals("FREE")){
            logger.info("Flight {} Seat {}{} is {}", flightCode,row,column,ret);
        }
        else{
            logger.info("Flight {} Seat {}{} is occupied by {}",flightCode,row,column,ret);
        }
        return ret;
        
    }

    @Override
    public void assignNewSeatToPassenger(String flightCode, String passengerName, int row, char column)  throws RemoteException, FlightDoesntExistException, FlightIsNotPendingException, PassengerDoesntHaveTicketException, PassengerDoesntHaveTicketException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException {
        fm.assignNewSeatToPassenger(flightCode, passengerName, row, column);
        logger.info("Assigned seat {}{} for passenger {} in flight {}", row, column, passengerName, flightCode);
    }

    @Override
    public void movePassengerToNewSeat(String flightCode, String passengerName, int row, char column) throws RemoteException, FlightDoesntExistException, FlightIsNotPendingException, PassengerIsAlreadySeatedException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException, PassengerIsNotSeatedException, PassengerDoesntHaveTicketException {
        fm.resitPassenger(flightCode, passengerName, row, column);
        logger.info("Passenger {} of flight {} was move to seat {}{}", passengerName, flightCode, row, column);
    }

    @Override
    public Map<String, List<Seat>> listAlternativeFlightSeats(String flightCode, String passengerName) throws RemoteException, FlightDoesntExistException, PassengerDoesntHaveTicketException {
        return fm.listAlternativeFlightSeats(flightCode, passengerName);
        //logger.info("Listing alternative flights for {}", passengerName);
    }

    @Override
    public void changePassengerFlight(String passengerName, String oldFlightCode, String newFlightCode)  throws RemoteException, FlightDoesntExistException, FlightIsNotPendingException,TicketNotInFlightException, FlightIsNotAnAlternativeException{
        fm.changePassengerFlight(passengerName, oldFlightCode, newFlightCode);
        logger.info("Changed {}'s flight {} to {}", passengerName, oldFlightCode,newFlightCode);
    }

    @Override
    public List<Seat> consultSeatMap(String flightCode)  throws RemoteException, FlightDoesntExistException {
        return fm.consultSeatMap(flightCode);
       // logger.info("SeatMap for flight {}", flightCode);
    }

    @Override
    public List<Seat> consultSeatMap(String flightCode, SeatCategory category)  throws RemoteException, FlightDoesntExistException, SeatCategoryDoesntExistException {
        return fm.consultSeatMap(flightCode, category);
       // logger.info("SeatMap for flight {} with category {}", flightCode, category);
    }

    @Override
    public List<Seat> consultSeatMap(String flightCode, int row)  throws RemoteException, FlightDoesntExistException, SeatRowDoesntExistException {
        return fm.consultSeatMap(flightCode, row);
       // logger.info("SeatMap for flight {} of row {}", flightCode, row);
    }
}
