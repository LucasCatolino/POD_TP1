package ar.edu.itba.pod.server;

import ar.edu.itba.pod.api.entities.*;
import ar.edu.itba.pod.api.exceptions.*;
import ar.edu.itba.pod.server.utils.Pair;

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
        this.flightsCancelled = new TreeSet<>();
    }

    ////////////////////////////////////////////////////
    //          FlightManagementService               //
    ////////////////////////////////////////////////////

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

    public FlightStatus checkFlightStatus(String flightCode) throws FlightDoesntExistException {
        Flight f = flightsMap.get(flightCode);
        if(f == null)
            throw new FlightDoesntExistException(flightCode);
        return f.getFlightStatus();
    }

    public void confirmFlight(String flightCode) throws FlightDoesntExistException {
        Flight f = flightsMap.get(flightCode);
        if(f == null)
            throw new FlightDoesntExistException(flightCode);
        synchronized (flightsMap) {
            f.confirmFlight();
            flightsMap.put(flightCode, f);
        }
    }

    public void cancelFlight(String flightCode) throws FlightDoesntExistException {
        Flight f = flightsMap.get(flightCode);
        if(f == null)
            throw new FlightDoesntExistException(flightCode);
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

    public List<Flight> getPendingFlightsByDestiny(String destiny){
        List<Flight> ret = new ArrayList<>();        
        for(Flight f : flightsMap.values()){
            if(f.getStatus() == FlightStatus.PENDING && f.getDestinyAirportCode().equals(destiny) && f.getCompletion()!= 1.0){
                ret.add(f);
            }
        }
        return ret;
    }

    public Pair<Flight,SeatCategory> getBestAlternativeFlight(List<Flight> flights, SeatCategory cat){
        Flight bestBusiness = null;
        Flight bestPremium = null;
        Flight bestEconomy = null;
        for (Flight f : flights){
            if(f.getbTickets() > 0) {
                if (bestBusiness == null || bestBusiness.getCompletion() > f.getCompletion()) {
                    bestBusiness = f;
                }
            }
            if(f.getEpTickets() > 0){
                if(bestPremium == null || bestPremium.getCompletion() > f.getCompletion()){
                    bestPremium = f;
                }
            }
            if(f.geteTickets() > 0){
                if(bestEconomy == null || bestEconomy.getCompletion() > f.getCompletion()){
                    bestEconomy = f;
                }
            }
        }
        if(cat == SeatCategory.BUSINESS && bestBusiness != null){
            return new Pair<>(bestBusiness, SeatCategory.BUSINESS);
        }
        if(bestPremium != null && cat != SeatCategory.ECONOMY){
            return new Pair<>(bestPremium, SeatCategory.PREMIUM_ECONOMY);
        }
        return new Pair<>(bestEconomy, SeatCategory.ECONOMY);
    }
    
    public void changeTicket(Flight oldFlight, Flight newFlight, String passengerName, SeatCategory newCat) throws TicketNotInFlightException{
        for(Ticket t : oldFlight.getTickets()){
            if (t.getPassengerName().equals(passengerName)){
                oldFlight.getTickets().remove(t);
                oldFlight.subtractTicketCount(newCat);
                newFlight.getTickets().add(new Ticket(newCat, passengerName));
                newFlight.addTicketCount(newCat);

            }
        }
        throw new TicketNotInFlightException(oldFlight.getFlightCode(), passengerName);
    }

    public void forceTicketChange() throws TicketNotInFlightException {
        for(Flight cf : flightsCancelled) {
            List<Flight> alternativeFlights = getPendingFlightsByDestiny(cf.getDestinyAirportCode());
            for(Ticket t : cf.getTickets()){
                Pair<Flight, SeatCategory> pr = getBestAlternativeFlight(alternativeFlights, t.getCategory());
                changeTicket(cf, pr.first, t.getPassengerName(), pr.second);
            }
            flightsCancelled.remove(cf);
        }
    }

    ////////////////////////////////////////////////////
    //            FlightNotificationService           //
    ////////////////////////////////////////////////////


    ////////////////////////////////////////////////////
    //            SeatAssignmentService               //
    ////////////////////////////////////////////////////

    public boolean seatIsOccupied(String flightCode, int row, char column) throws FlightDoesntExistException, SeatDoesntExistException {
        Flight f = flightsMap.get(flightCode);
        if(f == null){
            throw new FlightDoesntExistException(flightCode);
        }
        for(Seat s : f.getSeats()){
            if(row == s.getRow() && column == s.getColumn()){
                return !s.getPassengerName().equals("");
            }
        }
        throw new SeatDoesntExistException(row,column);
    }


    public void assignNewSeatToPassenger(String flightCode, String passengerName, int row, char column) throws FlightDoesntExistException, FlightIsNotPendingException, PassengerDoesntHaveTicketException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException {
        Flight f = flightsMap.get(flightCode);
        if(f == null){
            throw new FlightDoesntExistException(flightCode);
        }
        if(f.getStatus() != FlightStatus.PENDING){
            throw new FlightIsNotPendingException(flightCode);
        }
        boolean passengerHasTicket = false;
        SeatCategory cat = null;
        for (Ticket t : f.getTickets()){
            if(t.getPassengerName().equals(passengerName)){
                passengerHasTicket = true;
                cat = t.getCategory();
                break;
            }
        }
        if(!passengerHasTicket){
            throw new PassengerDoesntHaveTicketException(passengerName,flightCode); 
        }

        for(Seat s : f.getSeats()){
            if(s.getPassengerName().equals(passengerName)){
                throw new PassengerIsAlreadySeatedException(passengerName,flightCode) ; 
            }
        }

        for(Seat s : f.getSeats()){
            if(row == s.getRow() && column == s.getColumn()){
                if(!s.getPassengerName().equals("")){
                    throw new SeatIsTakenException(flightCode, row, column); 
                } 
                if(s.getCategory().compareTo(cat) > 0){
                    throw new InvalidSeatCategoryException();
                }
                else{
                    s.setPassengerName(passengerName);
                    return;
                }
            }
        }
    }

    public void unsitPassenger( String flightCode,String passengerName) throws FlightDoesntExistException, FlightIsNotPendingException, PassengerIsNotSeatedException{
        Flight f = flightsMap.get(flightCode);
        if(f == null){
            throw new FlightDoesntExistException(flightCode);
        }
        if(f.getStatus() != FlightStatus.PENDING){
            throw new FlightIsNotPendingException(flightCode);
        }
        for(Seat s : f.getSeats()){
            if(s.getPassengerName().equals(passengerName)){
                s.setPassengerName("");
                return;
            }
        }
        throw new PassengerIsNotSeatedException(passengerName,flightCode);
    }

    public void resitPassenger(String flightCode, String passengerName, int row, char column) throws FlightDoesntExistException, FlightIsNotPendingException, PassengerDoesntHaveTicketException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException, PassengerIsNotSeatedException{
        unsitPassenger(flightCode,passengerName);
        assignNewSeatToPassenger(flightCode, passengerName, row, column);
    }

    public Map<String, List<Seat>> listAlternativeFlightSeats(String flightCode, String passengerName) throws FlightDoesntExistException, PassengerDoesntHaveTicketException{
        Flight flight = flightsMap.get(flightCode);
        if(flight == null){
            throw new FlightDoesntExistException(flightCode);
        }
        boolean passengerExists = false;
        for(Ticket t : flight.getTickets()){
            if(t.getPassengerName().equals(passengerName)){
                passengerExists = true;
            }
        }
        if(!passengerExists){
            throw new PassengerDoesntHaveTicketException(passengerName, flightCode);
        }
        Map<String, List<Seat>> ret = new HashMap<>();
        List<Flight> alternativeFlights = getPendingFlightsByDestiny(flightsMap.get(flightCode).getDestinyAirportCode());
        for(Flight f : alternativeFlights){
            List<Seat> seats = new ArrayList<>();
            for(Seat s : f.getSeats()){
                if(s.getPassengerName().equals("")){
                    seats.add(s);
                }
            }
            ret.put(f.getFlightCode(),seats);
        }
        return ret;
    }

    public void changePassengerFlight(String passengerName, String oldFlightCode, String newFlightCode) throws FlightDoesntExistException, FlightIsNotPendingException, TicketNotInFlightException, FlightIsNotAnAlternativeException{
        Flight oldFlight = flightsMap.get(oldFlightCode);
        if(oldFlight == null){
            throw new FlightDoesntExistException(oldFlightCode);
        }
        Flight newFlight = flightsMap.get(newFlightCode);
        if(newFlight == null){
            throw new FlightDoesntExistException(newFlightCode);
        }
        if(newFlight.getStatus() != FlightStatus.PENDING){
            throw new FlightIsNotPendingException(newFlightCode);
        }
        List<Flight> alternativeFlights = getPendingFlightsByDestiny(oldFlight.getDestinyAirportCode());
        for(Flight f : alternativeFlights){
            if(f.getFlightCode().equals(newFlight.getFlightCode())){
                for(Ticket t : f.getTickets()){
                    if(t.getPassengerName().equals(passengerName)){
                        if(t.getCategory() == SeatCategory.BUSINESS && newFlight.getbTickets() > 0){
                            changeTicket(oldFlight, newFlight, passengerName, SeatCategory.BUSINESS);
                        }
                        else if(t.getCategory() != SeatCategory.ECONOMY && newFlight.getEpTickets() >0){
                            changeTicket(oldFlight,newFlight, passengerName, SeatCategory.PREMIUM_ECONOMY);
                        }
                        else {
                            changeTicket(oldFlight,newFlight, passengerName, SeatCategory.ECONOMY);
                        }
                    }
                }
                return;
            }
        }
        throw new FlightIsNotAnAlternativeException(newFlightCode,passengerName);
    }

    ////////////////////////////////////////////////////
    //           SeatMapConsultationService           //
    ////////////////////////////////////////////////////
}

/* Comentarios de Nava
 * 
 */