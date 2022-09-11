package ar.edu.itba.pod.server;

import ar.edu.itba.pod.api.entities.*;
import ar.edu.itba.pod.api.exceptions.FlightAlreadyExistsException;
import ar.edu.itba.pod.api.exceptions.FlightDoesntExistsException;
import ar.edu.itba.pod.api.exceptions.PlaneModelDoesntExistsException;
import ar.edu.itba.pod.api.exceptions.TicketNotInFlightException;

import java.util.TreeSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class FlightsManagement {
    private Map<String, Flight> flightsMap;
    private List<PlaneModel> planeModels;
    private SortedSet<Flight> flightsCancelled;

    private final ReadWriteLock flightsMapLock;


    public FlightsManagement() {
        this.flightsMap = new HashMap<>();
        this.planeModels = new ArrayList<>();
        this.flightsMapLock = new ReentrantReadWriteLock();
        this.flightsCancelled = new TreeSet();
    }

    public void createNewPlaneModel(String name, int brows, int bcols, int eprows, int epcols, int erows, int ecols) throws PlaneModelDoesntExistsException {
        PlaneModel planeModelToAdd = new PlaneModel(name,brows,bcols,eprows,epcols,erows,ecols);
        synchronized(planeModels){
            if(planeModels.contains(planeModelToAdd)){
                throw new PlaneModelDoesntExistsException(planeModelToAdd.getName());
            }
            planeModels.add(planeModelToAdd);
        }
    }

    public void createNewFlight(String planeModel, String flightCode, String dstCode, SortedSet<Ticket> passengers) throws PlaneModelDoesntExistsException{
        try {
            Flight f = new Flight(getModel(planeModel), flightCode, dstCode, passengers);
            synchronized (flightsMap) {
                if(!checkIfFlightExists(flightCode))
                    flightsMap.put(flightCode, f);
            }
        } catch (PlaneModelDoesntExistsException e) {
            System.out.println(e);
        } catch (FlightAlreadyExistsException e) {
            System.out.println(e);
        }
    }

    public FlightStatus checkFlightStatus(String flightCode) throws FlightDoesntExistsException {
        Flight f = flightsMap.get(flightCode);
        if(f == null)
            throw new FlightDoesntExistsException(flightCode);
        return f.checkFlightStatus();
    }

    public void confirmFlight(String flightCode) throws FlightDoesntExistsException {
        Flight f = flightsMap.get(flightCode);
        if(f == null)
            throw new FlightDoesntExistsException(flightCode);
        synchronized (flightsMap) {
            flightsMap.put(flightCode, f.confirmFlight());
        }
    }

    public void cancelFlight(String flightCode) throws FlightDoesntExistsException {
        Flight f = flightsMap.get(flightCode);
        if(f == null)
            throw new FlightDoesntExistsException(flightCode);
        synchronized (flightsMap) {
            flightsMap.put(flightCode, f.cancelFlight());
        }
    }

    private boolean checkIfFlightExists(String flightCode) throws FlightAlreadyExistsException{
        if(flightsMap.containsKey(flightCode))
            throw new FlightAlreadyExistsException(flightCode);
        return false;
    }

    private PlaneModel getModel(String name) throws PlaneModelDoesntExistsException {
        for(PlaneModel pm : planeModels)
            if(pm.getName().equals(name))
                return pm;
        throw new PlaneModelDoesntExistsException(name);
    }

    public Flight findFlightByCode(String flightCode){
        flightsMapLock.readLock().lock();
        try {
            return flightsMap.get(flightCode);
        }finally {
            flightsMapLock.readLock().unlock();
        }
    }

    // public List<Flight> getCancelledFlights(){
    //     List<Flight> cancelled = new ArrayList<>();
    //     for(Flight f : flightsMap.values()){
    //         if(f.getStatus() == FlightStatus.CANCELED){
    //             cancelled.add(f);
    //         }
    //     }
    //     return cancelled;
    // }

    public List<Flight> getPendingFlightsByDestiny(String destiny){
        List<Flight> ret = new ArrayList<>();        
        for(Flight f : flightsMap.values()){
            if(f.getStatus() == FlightStatus.PENDING && f.getDestinyAirportCode().equals(destiny)){
                ret.add(f);
            }
        }
        return ret;
    }

    public int getAvailableSeatCount(Flight f, SeatCategory cat){
        int ret = 0;
        for(Seat s : f.getSeats()){
            if((s.getCategory().equals(cat)) && s.getPassengerName().equals("")){
                ret++;
            }
        }
        return ret;
    }

    public Flight getBestAlternativeFlight(List<Flight> flights, SeatCategory cat){

    }
    
    public void changeTicket(Flight oldFlight, Flight newFlight, String passengerName,SeatCategory newCat) throws TicketNotInFlightException{
        for(Ticket t : oldFlight.getTickets()){
            if (t.getPassengerName().equals(passengerName)){
                oldFlight.getTickets().remove(t);
                newFlight.getTickets().add(new Ticket(newCat,passengerName));
                return;
            }
        }
        throw new TicketNotInFlightException(oldFlight.getFlightCode(),passengerName);
    }

    public void forceTicketChange(){
        for(Flight cf : flightsCancelled) {
            List<Flight> alternativeFlights = getPendingFlightsByDestiny(cf.getDestinyAirportCode());
            // Aca habria que reubicar todos los pasajeros del vuelo cf dentro de alternativeFlights
            flightsCancelled.remove(cf);
        }
    }
}

