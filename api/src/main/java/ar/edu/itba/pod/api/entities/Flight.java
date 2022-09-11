package ar.edu.itba.pod.api.entities;

import java.util.SortedSet;
import java.util.ArrayList;
import java.util.List;

public class Flight implements Comparable {
    // Variables
    PlaneModel model;
    List<Seat> seats;
    SortedSet<Ticket> tickets;
    FlightStatus status;
    String flightCode;
    String destinyAirportCode;
    
    // Constructor
    public Flight(PlaneModel model, String flightCode, String destinyAirportCode, SortedSet<Ticket> passengers) {
        this.model = model;
        this.seats = generateFlightSeats();
        this.tickets = passengers;
        this.status = FlightStatus.PENDING;
        this.flightCode = flightCode;
        this.destinyAirportCode = destinyAirportCode;
    }
    // Metodos
    private List<Seat> generateFlightSeats(){
        List<Seat> res = new ArrayList<>();
        // Business Seats
        for(int rows = 0 ; rows < this.model.getBusinessRows() ; rows++)
            for(int cols = 0 ; cols < this.model.getBusinessCols() ; cols++)
                res.add(new Seat(rows, (char)(cols + 'A'), SeatCategory.BUSINESS));
        // Premium Economy Seats
        for(int rows = 0 ; rows < this.model.getEpRows() ; rows++)
            for(int cols = 0 ; cols < this.model.getEpCols() ; cols++)
                res.add(new Seat(rows, (char)(cols + 'A'), SeatCategory.PREMIUM_ECONOMY));
        // Economy Seats
        for(int rows = 0 ; rows < this.model.getEconRows() ; rows++)
            for(int cols = 0 ; cols < this.model.getEconCols() ; cols++)
                res.add(new Seat(rows, (char)(cols + 'A'), SeatCategory.ECONOMY));
        return res;
    }
    public FlightStatus getFlightStatus() { return this.status; }
    public void confirmFlight() { this.status = FlightStatus.CONFIRMED; }
    
    /*private void assignSeats() {
        int aux = 0;
        for(Ticket t : this.passengers) {
            for(int i = aux ; i < seats.size() ; i++) {
                Seat s = seats.get(i);
                if(s.getCategory().equals(t.getCategory()) && !s.getPassengerName().equals("UN/A")) {
                    s.setPassengerName(t.getPassengerName());
                    aux = i;
                    break;
                }
            }
        }
    }*/


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
}
