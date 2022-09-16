
package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.api.entities.*;
import ar.edu.itba.pod.api.exceptions.FlightAlreadyExistsException;
import ar.edu.itba.pod.api.exceptions.FlightDoesntExistException;
import ar.edu.itba.pod.api.exceptions.FlightIsNotPendingException;
import ar.edu.itba.pod.api.exceptions.InvalidSeatCategoryException;
import ar.edu.itba.pod.api.exceptions.PassengerDoesntHaveTicketException;
import ar.edu.itba.pod.api.exceptions.PassengerIsAlreadySeatedException;
import ar.edu.itba.pod.api.exceptions.PassengerNotInFlightException;
import ar.edu.itba.pod.api.exceptions.PlaneModelAlreadyExistsException;
import ar.edu.itba.pod.api.exceptions.PlaneModelDoesntExistsException;
import ar.edu.itba.pod.api.exceptions.SeatDoesntExistException;
import ar.edu.itba.pod.api.exceptions.SeatIsTakenException;
import ar.edu.itba.pod.api.exceptions.TicketNotInFlightException;
import ar.edu.itba.pod.server.FlightsManagement;
import static org.junit.Assert.*;


import org.junit.Test;


import java.util.*;


public class FlightsTest {

    private static final FlightsManagement fm = new FlightsManagement();

    @Test
    public void testConfirm() throws PlaneModelAlreadyExistsException, PlaneModelDoesntExistsException, FlightAlreadyExistsException, FlightDoesntExistException, FlightIsNotPendingException{
        fm.createNewPlaneModel("ATT5", 5, 5, 5, 5, 5, 5);
        fm.createNewFlight("ATT5", "AA995", "JFK", new TreeSet<>());
        Flight f = fm.getFlightsMap().get("AA995");
        assertEquals(FlightStatus.PENDING, f.getStatus());
        fm.confirmFlight(f.getFlightCode());
        assertEquals(FlightStatus.CONFIRMED, f.getStatus());
    }

    @Test
    public void testCancel() throws PlaneModelAlreadyExistsException, PlaneModelDoesntExistsException, FlightAlreadyExistsException, FlightDoesntExistException, FlightIsNotPendingException{
        fm.createNewPlaneModel("ATT6", 5, 5, 5, 5, 5, 5);
        fm.createNewFlight("ATT6", "AA996", "JFK", new TreeSet<>());
        Flight f = fm.getFlightsMap().get("AA996");
        assertEquals(FlightStatus.PENDING, f.getStatus());
        fm.cancelFlight(f.getFlightCode());
        assertEquals(FlightStatus.CANCELED, f.getStatus());
    }

    @Test(expected = FlightIsNotPendingException.class)
    public void testConfirmNotPendingFlight() throws FlightIsNotPendingException, PlaneModelAlreadyExistsException, PlaneModelDoesntExistsException, FlightAlreadyExistsException, FlightDoesntExistException {
        fm.createNewPlaneModel("ATT2", 5, 5, 5, 5, 5, 5);
        
        SortedSet<Ticket> ts = new TreeSet<>();
        ts.add(new Ticket(SeatCategory.BUSINESS, "SANTI"));

        fm.createNewFlight("ATT2", "AA990", "JFK", ts);
        Flight f = fm.getFlightsMap().get("AA990");
        assertEquals(FlightStatus.PENDING, f.getStatus());

        fm.confirmFlight(f.getFlightCode());

        assertEquals(FlightStatus.CONFIRMED, f.getStatus());

        fm.confirmFlight(f.getFlightCode());
        
    }

    @Test(expected = FlightIsNotPendingException.class)
    public void testCancelNotPendingFlight() throws FlightIsNotPendingException, PlaneModelAlreadyExistsException, PlaneModelDoesntExistsException, FlightAlreadyExistsException, FlightDoesntExistException {
        fm.createNewPlaneModel("ATT3", 5, 5, 5, 5, 5, 5);
        fm.createNewFlight("ATT3", "AA991", "JFK", new TreeSet<>());
        Flight f = fm.getFlightsMap().get("AA991");
        assertEquals(FlightStatus.PENDING, f.getStatus());

        fm.confirmFlight(f.getFlightCode());

        assertEquals(FlightStatus.CONFIRMED, f.getStatus());

        fm.cancelFlight(f.getFlightCode());
    }

    @Test
    public void testAssignSeat() throws FlightIsNotPendingException, PlaneModelAlreadyExistsException, PlaneModelDoesntExistsException, FlightAlreadyExistsException, FlightDoesntExistException, PassengerDoesntHaveTicketException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException, SeatDoesntExistException {
        fm.createNewPlaneModel("ATT4", 5, 5, 5, 5, 5, 5);
        SortedSet<Ticket> tickets = new TreeSet<>();
        tickets.add(new Ticket(SeatCategory.BUSINESS,"Joaco"));
        fm.createNewFlight("ATT4", "AA992", "JFK", tickets);
        Flight f = fm.getFlightsMap().get("AA992");
        int row = 1;
        char column = 'A';
        Seat seat = null;
        for(Seat s : f.getSeats()){
            if(s.getRow() == row && s.getColumn() == column){
                seat = s;
            }
        }   
        assertEquals("FREE", seat.getPassengerName());
        fm.assignNewSeatToPassenger("AA992", "Joaco", row, column);
        assertEquals("Joaco", seat.getPassengerName());
    }

    @Test 
    public void testSeatIsOcuppied() throws PlaneModelAlreadyExistsException, PlaneModelDoesntExistsException, FlightAlreadyExistsException, FlightDoesntExistException, FlightIsNotPendingException, PassengerDoesntHaveTicketException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException, SeatDoesntExistException{
        fm.createNewPlaneModel("ATT7", 5, 5, 5, 5, 5, 5);
        SortedSet<Ticket> tickets = new TreeSet<>();
        tickets.add(new Ticket(SeatCategory.BUSINESS,"Santi"));
        fm.createNewFlight("ATT7", "AA993", "JFK", tickets);
        Flight f = fm.getFlightsMap().get("AA993");
        int row = 1;
        char column = 'A'; 
        assertEquals("FREE", fm.seatIsOccupied("AA993", row, column));
        fm.assignNewSeatToPassenger("AA993", "Santi", row, column);
        assertEquals("Santi", fm.seatIsOccupied("AA993", row, column));
    }

    @Test
    public void testChangeTicket() throws TicketNotInFlightException, PassengerNotInFlightException, FlightDoesntExistException, PlaneModelDoesntExistsException, FlightAlreadyExistsException, PlaneModelAlreadyExistsException{
        fm.createNewPlaneModel("ATT9", 5, 5, 5, 5, 5, 5);
        SortedSet<Ticket> tickets = new TreeSet<>();
        Ticket t = new Ticket(SeatCategory.BUSINESS,"Santi");
        tickets.add(t);
        fm.createNewFlight("ATT9", "AA880", "JFK", tickets);
        fm.createNewFlight("ATT9", "AA881", "JFK", new TreeSet<>());

        assertFalse(fm.getFlightsMap().get("AA881").getTickets().contains(t));
        fm.changeTicket(fm.getFlightsMap().get("AA880"), fm.getFlightsMap().get("AA881"), "Santi", SeatCategory.BUSINESS);
        assertTrue(fm.getFlightsMap().get("AA881").getTickets().contains(t));
    }
}

