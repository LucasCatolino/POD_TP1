package ar.edu.itba.pod.api.entities;

import java.io.Serializable;

public class Ticket implements Comparable, Serializable{
    private SeatCategory category;
    private String passengerName;

    public Ticket(SeatCategory category, String passengerName) {
        this.category = category;
        this.passengerName = passengerName;
    }

    public SeatCategory getCategory() {
        return category;
    }

    public void setCategory(SeatCategory category) {
        this.category = category;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }
    @Override
    public int compareTo(Object o) {
        Ticket t = (Ticket) o;
        return this.passengerName.compareTo(t.passengerName);
    }
    
}
