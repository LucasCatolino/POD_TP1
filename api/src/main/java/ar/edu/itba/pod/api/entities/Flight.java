package ar.edu.itba.pod.api.entities;

import java.util.Collection;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Flight implements Comparable, Serializable {
    // Variables
    PlaneModel model;
    List<Seat> seats;
    SortedSet<Ticket> tickets;
    FlightStatus status;
    String flightCode;
    String destinyAirportCode;
    int bTickets = 0, epTickets = 0, eTickets = 0;

    // Constructor
    public Flight(PlaneModel model, String flightCode, String destinyAirportCode, SortedSet<Ticket> passengers) {
        this.model = model;
        this.flightCode = flightCode;
        this.seats = generateFlightSeats();
        this.tickets = passengers;
        this.status = FlightStatus.PENDING;
        this.destinyAirportCode = destinyAirportCode;
        countTicketsByCategory(passengers);
    }

    private void countTicketsByCategory(Collection<Ticket> tickets) {
        for (Ticket t : tickets) {
            if (t.getCategory() == SeatCategory.BUSINESS) {
                this.bTickets++;
            } else if (t.getCategory() == SeatCategory.PREMIUM_ECONOMY) {
                this.epTickets++;
            } else if (t.getCategory() == SeatCategory.ECONOMY) {
                this.eTickets++;
            }
        }
    }
    
    // Metodos

    public boolean passengerInFlight(String n) {
        for(Ticket t : tickets) 
            if(t.getPassengerName().equals(n))
                return true;
        return false;
    }

    private List<Seat> generateFlightSeats() {
        List<Seat> res = new ArrayList<>();
        // Business Seats
        int rows=1; 
        
        for (int i=0; i < this.model.getBusinessRows(); i++,rows++){
            for (int cols = 0; cols < this.model.getBusinessCols(); cols++){
                res.add(new Seat(rows, (char) (cols + 'A'), SeatCategory.BUSINESS));
            }
        }
        // Premium Economy Seats
        for (int i=0; i < this.model.getEpRows(); i++,rows++){
            for (int cols = 0; cols < this.model.getEpCols(); cols++){
                res.add(new Seat(rows, (char) (cols + 'A'), SeatCategory.PREMIUM_ECONOMY));
            }
        }
        // Economy Seats
        for (int i=0; i < this.model.getEconRows(); i++, rows++){
            for (int cols = 0; cols < this.model.getEconCols(); cols++){
                res.add(new Seat(rows, (char) (cols + 'A'), SeatCategory.ECONOMY));
            }
        }

        return res;
    }

    public FlightStatus getFlightStatus() {
        return this.status;
    }

    public void confirmFlight() {
        this.status = FlightStatus.CONFIRMED;
    }

    public SortedSet<Ticket> getTickets() {
        return tickets;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public String getDestinyAirportCode() {
        return destinyAirportCode;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public Flight cancelFlight() {
        this.status = FlightStatus.CANCELED;
        return this;
    }

    @Override
    public int compareTo(Object o) {
        Flight f = (Flight) o;
        return flightCode.compareTo((f.getFlightCode()));
    }

    public void addTicketCount(SeatCategory cat){
        if(cat == SeatCategory.BUSINESS){
            bTickets++;
            return;
        }
        if(cat == SeatCategory.ECONOMY){
            eTickets++;
            return;
        }
        if(cat == SeatCategory.PREMIUM_ECONOMY)
            epTickets++;
    }

    public void subtractTicketCount(SeatCategory cat){
        if(cat == SeatCategory.BUSINESS){
            bTickets--;
            return;
        }
        if(cat == SeatCategory.ECONOMY){
            eTickets--;
            return;
        }
        if(cat == SeatCategory.PREMIUM_ECONOMY)
            epTickets--;

    }

    public double getCompletion(){
        return ((double) (epTickets+bTickets+eTickets)) / getModel().getTotalSeats();
    }

    public PlaneModel getModel() {
        return model;
    }

    public int getbTickets() {
        return model.getBusinessSeats() - bTickets;
    }

    public int getEpTickets() {
        return model.getEconomyPremiumSeats() - epTickets;
    }

    public int geteTickets() {
        return model.getEconomySeats() - eTickets;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "Flight " + flightCode + " with destination " + destinyAirportCode;
    }
}
