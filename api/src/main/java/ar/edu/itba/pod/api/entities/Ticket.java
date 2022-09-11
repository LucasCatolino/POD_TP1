package ar.edu.itba.pod.api.entities;

public class Ticket implements Comparable{
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

    public int compareTo(Ticket t){
        return this.passengerName.compareTo(t.passengerName);
    }
    
}
