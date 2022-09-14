package ar.edu.itba.pod.server;

import ar.edu.itba.pod.api.entities.*;
import ar.edu.itba.pod.api.exceptions.*;
import ar.edu.itba.pod.api.utils.Pair;

import java.util.TreeSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;


public class FlightsManagement {
    private Map<String, Flight> flightsMap;
    private List<PlaneModel> planeModels;
    private SortedSet<Flight> flightsCancelled;

    private final ReadWriteLock flightsMapLock;
    private final ReadWriteLock planeModelsLock;
    private final ReadWriteLock flightsCancelledLock;


    public FlightsManagement() {
        this.flightsMap = new HashMap<>();
        this.planeModels = new ArrayList<>();
        this.flightsMapLock = new ReentrantReadWriteLock();
        this.planeModelsLock = new ReentrantReadWriteLock();
        this.flightsCancelledLock = new ReentrantReadWriteLock();
        this.flightsCancelled = new TreeSet<>();
    }

    ////////////////////////////////////////////////////
    //          FlightManagementService               //
    ////////////////////////////////////////////////////
    public void createNewPlaneModel(String name, int brows, int bcols, int eprows, int epcols, int erows, int ecols) throws PlaneModelAlreadyExistsException  {
        PlaneModel planeModelToAdd = new PlaneModel(name,brows,bcols,eprows,epcols,erows,ecols);
        planeModelsLock.writeLock().lock();
        try{
            if(planeModels.contains(planeModelToAdd)){
                throw new PlaneModelAlreadyExistsException(planeModelToAdd.getName());
            }
            planeModels.add(planeModelToAdd);
        }
        finally{
            planeModelsLock.writeLock().unlock();
        }
    }

    public void createNewFlight(String planeModel, String flightCode, String dstCode, SortedSet<Ticket> passengers) throws PlaneModelDoesntExistsException, FlightAlreadyExistsException{
        flightsMapLock.writeLock().lock();
        try {
            Flight f = new Flight(getModel(planeModel), flightCode, dstCode, passengers);
            synchronized(f){
                if(!checkIfFlightExists(flightCode))
                    flightsMap.put(flightCode, f);
            }
        }finally{
            flightsMapLock.writeLock().unlock();
        }
    }

    public FlightStatus checkFlightStatus(String flightCode) throws FlightDoesntExistException {
        flightsMapLock.readLock().lock();
        try{
            Flight f = flightsMap.get(flightCode);
            if(f == null)
                    throw new FlightDoesntExistException(flightCode);
            synchronized(f){
                return f.getFlightStatus();
            }
        }finally{
            flightsMapLock.readLock().unlock();
        }
    }

    public void confirmFlight(String flightCode) throws FlightDoesntExistException {
        flightsMapLock.readLock().lock();
        try {
            Flight f = flightsMap.get(flightCode);
            if(f == null)
                throw new FlightDoesntExistException(flightCode);
            synchronized (flightsMap) {
                f.confirmFlight();
                flightsMap.put(flightCode, f);
            }
        } finally {
            flightsMapLock.readLock().unlock();
        }
    }

    public void cancelFlight(String flightCode) throws FlightDoesntExistException {
        flightsMapLock.readLock().lock();
        flightsCancelledLock.writeLock().lock();
        try {
            Flight f = flightsMap.get(flightCode);
            if(f == null)
                    throw new FlightDoesntExistException(flightCode);
            synchronized(f){
                flightsMap.put(flightCode, f.cancelFlight());
                flightsCancelled.add(f);
            }            
        } finally {
            flightsMapLock.readLock().unlock();
            flightsCancelledLock.writeLock().unlock();
        }
    }

    private boolean checkIfFlightExists(String flightCode) throws FlightAlreadyExistsException{
        flightsMapLock.readLock().lock();
        try {
            if(flightsMap.containsKey(flightCode))
                throw new FlightAlreadyExistsException(flightCode);
            return false;
        }finally{
            flightsMapLock.readLock().unlock();
        }
    }

    private PlaneModel getModel(String name) throws PlaneModelDoesntExistsException {
        planeModelsLock.readLock().lock();
        try{
            for(PlaneModel pm : planeModels)
                if(pm.getName().equals(name))
                    return pm;
            throw new PlaneModelDoesntExistsException(name);
        } finally{
            planeModelsLock.readLock().unlock();
        }
    }


    public List<Flight> getPendingFlightsByDestiny(String destiny){
        List<Flight> ret = new ArrayList<>();
        //TODO: checkiar si hay que sincronizar ret
        flightsMapLock.readLock().lock();
        try{
            for(Flight f : flightsMap.values()){
                synchronized(f){
                    if(f.getStatus() == FlightStatus.PENDING && f.getDestinyAirportCode().equals(destiny) && f.getCompletion() < 1.0){
                        ret.add(f);
                    }
                }
            }
            return ret;
        }finally{
            flightsMapLock.readLock().unlock();
        }
    }

    public Pair<Flight,SeatCategory> getBestAlternativeFlight(List<Flight> flights, SeatCategory cat){
        if(flights.isEmpty()){
            return new Pair<>(null, null);
        }
        Flight bestBusiness = null;
        Flight bestPremium = null;
        Flight bestEconomy = null;
        
        for (Flight f : flights){ 
            synchronized(f){
                if(f.getbTickets() > 0) {
                    if (bestBusiness == null){
                        bestBusiness = f;
                    }
                    else{
                        synchronized(bestBusiness){
                            if(bestBusiness.getCompletion() > f.getCompletion()) {
                                bestBusiness = f;
                            }
                        }
                    }
                }
                if(f.getEpTickets() > 0){
                    if (bestPremium == null){
                        bestPremium = f;
                    }
                    else{
                        synchronized(bestPremium){
                            if(bestPremium.getCompletion() > f.getCompletion()) {
                                bestPremium = f;
                            }
                        }
                    }
                }
                if(f.geteTickets() > 0){
                    if (bestEconomy == null){
                        bestEconomy = f;
                    }
                    else{
                        synchronized(bestEconomy){
                            if(bestEconomy.getCompletion() > f.getCompletion()) {
                                bestEconomy = f;
                            }
                        }
                    }
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
        synchronized(oldFlight){
            for(Ticket t : oldFlight.getTickets()){
                synchronized(newFlight){
                    synchronized(t){
                        if (t.getPassengerName().equals(passengerName)){
                            oldFlight.getTickets().remove(t);
                            oldFlight.subtractTicketCount(newCat);
                            newFlight.getTickets().add(new Ticket(newCat, passengerName));
                            newFlight.addTicketCount(newCat);
                            return;
                        }
                    }
                }
            }
            throw new TicketNotInFlightException(oldFlight.getFlightCode(), passengerName);
        }
    }

    public Pair<Integer,Map<String,List<String>>> forceTicketChange() throws TicketNotInFlightException {
        Map<String, List<String>> map = new HashMap<>();
        int ticketsChanged = 0;
        flightsCancelledLock.readLock().lock();
        try{
            for(Flight cf : flightsCancelled) {
                synchronized(cf){
                    List<Flight> alternativeFlights = getPendingFlightsByDestiny(cf.getDestinyAirportCode());
                    List<String> ps = new ArrayList<>();
                    for(Ticket t : cf.getTickets()){
                        synchronized(t){
                            //Todo: PR fijarse sis puede causar problema
                            Pair<Flight, SeatCategory> pr = getBestAlternativeFlight(alternativeFlights, t.getCategory());
                            if(pr.first == null){
                                ps.add(t.getPassengerName());
                            } else {
                                changeTicket(cf, pr.first, t.getPassengerName(), pr.second);
                                ticketsChanged++;
                            }
                        }
                    }
                    if(!ps.isEmpty()){
                        map.put(cf.getFlightCode(), ps);
                    }
                }
            }
            return new Pair<>(ticketsChanged, map);
        }finally{
            flightsCancelledLock.readLock().unlock();
        }
    }

    ////////////////////////////////////////////////////
    //            FlightNotificationService           //
    ////////////////////////////////////////////////////todo
    
        //TODO:

    ////////////////////////////////////////////////////
    //            SeatAssignmentService               //
    ////////////////////////////////////////////////////

    public String seatIsOccupied(String flightCode, int row, char column) throws FlightDoesntExistException, SeatDoesntExistException {
        Flight f = flightsMap.get(flightCode);
        if(f == null){
            throw new FlightDoesntExistException(flightCode);
        }
        synchronized(f){
            for(Seat s : f.getSeats()){
                synchronized(s){
                    if(row == s.getRow() && column == s.getColumn()){
                        return s.getPassengerName();
                    }
                }
            }
            throw new SeatDoesntExistException(row,column);
        }
    }


    public void assignNewSeatToPassenger(String flightCode, String passengerName, int row, char column) throws FlightDoesntExistException, FlightIsNotPendingException, PassengerDoesntHaveTicketException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException {
        Flight f = flightsMap.get(flightCode);
        if(f == null){
            throw new FlightDoesntExistException(flightCode);
        }
        synchronized(f){
            if(f.getStatus() != FlightStatus.PENDING){
                throw new FlightIsNotPendingException(flightCode);
            }
            boolean passengerHasTicket = false;
            SeatCategory cat = null;
            for (Ticket t : f.getTickets()){
                synchronized(t){
                    if(t.getPassengerName().equals(passengerName)){
                        passengerHasTicket = true;
                        cat = t.getCategory();
                        break;
                    }
                }
            }
            if(!passengerHasTicket){
                throw new PassengerDoesntHaveTicketException(passengerName,flightCode); 
            }

            for(Seat s : f.getSeats()){
                synchronized(s){
                    if(s.getPassengerName().equals(passengerName)){
                        throw new PassengerIsAlreadySeatedException(passengerName,flightCode) ; 
                    }
                }
            }

            for(Seat s : f.getSeats()){
                synchronized(s){
                    if(row == s.getRow() && column == s.getColumn()){
                        if(!s.getPassengerName().equals("FREE")){
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
                s.setPassengerName("FREE");
                return;
            }
        }
        throw new PassengerIsNotSeatedException(passengerName,flightCode);
    }

    public void resitPassenger(String flightCode, String passengerName, int row, char column) throws FlightDoesntExistException, FlightIsNotPendingException, PassengerDoesntHaveTicketException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException, PassengerIsNotSeatedException{
        unsitPassenger(flightCode,passengerName);
        assignNewSeatToPassenger(flightCode, passengerName, row, column);
    }

    public List<Flight> listAlternativeFlightSeats(String flightCode, String passengerName) throws FlightDoesntExistException, PassengerDoesntHaveTicketException{
        flightsMapLock.readLock().lock();
        try{
            Flight flight = flightsMap.get(flightCode);
            if(flight == null){
                throw new FlightDoesntExistException(flightCode);
            }
            synchronized(flight){
                boolean passengerExists = false;
                for(Ticket t : flight.getTickets()){
                    synchronized(t){
                        if(t.getPassengerName().equals(passengerName)){
                            passengerExists = true;
                        }
                    }
                }
                if(!passengerExists){
                    throw new PassengerDoesntHaveTicketException(passengerName, flightCode);
                }
                return getPendingFlightsByDestiny(flightsMap.get(flightCode).getDestinyAirportCode());
            }
        }finally{
            flightsMapLock.readLock().unlock();
        }
    }

    public void changePassengerFlight(String passengerName, String oldFlightCode, String newFlightCode) throws FlightDoesntExistException, FlightIsNotPendingException, TicketNotInFlightException, FlightIsNotAnAlternativeException{
        flightsMapLock.readLock().lock();
        try{
            Flight oldFlight = flightsMap.get(oldFlightCode);
            if(oldFlight == null){
                throw new FlightDoesntExistException(oldFlightCode);
            }
            Flight newFlight = flightsMap.get(newFlightCode);
            if(newFlight == null){
                throw new FlightDoesntExistException(newFlightCode);
            }
            synchronized(newFlight){
                synchronized(oldFlight){
                    if(newFlight.getStatus() != FlightStatus.PENDING){
                        throw new FlightIsNotPendingException(newFlightCode);
                    }
                    List<Flight> alternativeFlights = getPendingFlightsByDestiny(oldFlight.getDestinyAirportCode());
                    for(Flight f : alternativeFlights){
                        synchronized(f){
                        if(f.getFlightCode().equals(newFlight.getFlightCode())){
                            for(Ticket t : f.getTickets()){
                                synchronized(t){
                                    if(t.getPassengerName().equals(passengerName)){
                                        if(t.getCategory() == SeatCategory.BUSINESS && newFlight.getbTickets() > 0){
                                            changeTicket(oldFlight, newFlight, passengerName, SeatCategory.BUSINESS);
                                        }
                                        else if(t.getCategory() != SeatCategory.ECONOMY && newFlight.getEpTickets() >0){
                                            changeTicket(oldFlight,newFlight, passengerName, SeatCategory.PREMIUM_ECONOMY);
                                        }
                                        else if(newFlight.geteTickets() > 0){
                                            changeTicket(oldFlight,newFlight, passengerName, SeatCategory.ECONOMY);
                                        }
                                    }
                                }
                            }
                            return;
                        }
                    }
                    throw new FlightIsNotAnAlternativeException(newFlightCode,passengerName);
                    }
                }
            }
        }
        finally{
            flightsMapLock.readLock().unlock();
        }
    }

    ////////////////////////////////////////////////////
    //           SeatMapConsultationService           //
    ////////////////////////////////////////////////////
    
    public List<Seat> consultSeatMap(String flightCode) throws FlightDoesntExistException{
        flightsMapLock.readLock().lock();
        try{
            Flight f = flightsMap.get(flightCode);
            if(f == null){
                throw new FlightDoesntExistException(flightCode);
            }
            synchronized(f){
                return f.getSeats();
            }
        }finally{
            flightsMapLock.readLock().unlock();
        }
    }

    public List<Seat> consultSeatMap(String flightCode, int row) throws FlightDoesntExistException, SeatRowDoesntExistException{
        flightsMapLock.readLock().lock();
        try{
            Flight f = flightsMap.get(flightCode);
            if(f == null){
                throw new FlightDoesntExistException(flightCode);
            }
            synchronized(f){
                List<Seat> toret = f.getSeats().stream().filter(s -> s.getRow() == row).collect(Collectors.toList());
                if(toret.size() > 0)
                    return toret;
                throw new SeatRowDoesntExistException(flightCode,row);
            }
        }finally{
            flightsMapLock.readLock().unlock();
        }
    }

    public List<Seat> consultSeatMap(String flightCode, SeatCategory cat) throws FlightDoesntExistException, SeatCategoryDoesntExistException{
        flightsMapLock.readLock().lock();
        try {
            Flight f = flightsMap.get(flightCode);
            if(f == null){
                throw new FlightDoesntExistException(flightCode);
            }
            synchronized(f){
                List<Seat> toret = f.getSeats().stream().filter(s-> s.getCategory() == cat).collect(Collectors.toList());
                if(toret.size() > 0)
                    return toret;
                throw new SeatCategoryDoesntExistException(flightCode, categoryToString(cat));
            }
        } finally {
            flightsMapLock.readLock().unlock();
        }
    }

    public String categoryToString(SeatCategory cat){
        if(SeatCategory.BUSINESS == cat){
            return new String("Business");
        }
        if (SeatCategory.PREMIUM_ECONOMY == cat){
            return new String("Premium Economy");
        }
        if (SeatCategory.ECONOMY == cat){
            return new String("Economy");
        }
        return null;
    }
}