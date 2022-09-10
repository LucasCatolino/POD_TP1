package ar.ed.itba.pod.server;

import ar.edu.itba.pod.api.entities.FlightStatus;
import ar.edu.itba.pod.api.entities.Seat;
import ar.edu.itba.pod.api.entities.SeatCategory;
import ar.edu.itba.pod.api.entities.Ticket;
import ar.edu.itba.pod.api.services.FlightManagementService;
import ar.edu.itba.pod.api.services.FlightNotificationService;
import ar.edu.itba.pod.api.services.SeatAssignmentService;
import ar.edu.itba.pod.api.services.SeatMapConsultationService;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Servant implements SeatMapConsultationService, FlightNotificationService, SeatAssignmentService, FlightManagementService {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    

    @Override
    public void addPlaneModel(String modelName, int businessRows, int businessCols, int epRows, int epCols, int econRows, int econCols) {

    }

    @Override
    public void addFlight(String planeModelName, String flightCode, String destinyAirportCode, List<Ticket> tickets) {

    }

    @Override
    public FlightStatus checkFlightStatus(String flightCode) {
        return null;
    }

    @Override
    public void confirmFlight(String flightCode) {

    }

    @Override
    public void cancelFlight(String flightCode) {

    }

    @Override
    public void forceTicketChange() {

    }

    @Override
    public void registerPassengerToNotify(String flightCode, String passengerName) {

    }

    @Override
    public boolean seatIsOccupied(String flightCode, int row, char column) {
        return false;
    }

    @Override
    public void assignNewSeatToPassenger(String flightCode, String passengerName, int row, char column) {

    }

    @Override
    public void movePassengerToNewSeat(String flightCode, String passengerName, int row, char column) {

    }

    @Override
    public List<Pair<String, List<Seat>>> listAlternativeFlightSeats(String flightCode, String passengerName) {
        return null;
    }

    @Override
    public void changePassengerFlight(String passengerName, String oldFlightCode, String newFlightCode) {

    }

    @Override
    public List<Seat> consultSeatMap(String flightCode) {
        return null;
    }

    @Override
    public List<Seat> consultSeatMap(String flightCode, SeatCategory category) {
        return null;
    }

    @Override
    public List<Seat> consultSeatMap(String flightCode, int row) {
        return null;
    }
}
