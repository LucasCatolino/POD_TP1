package ar.edu.itba.pod.server;

import ar.edu.itba.pod.api.entities.*;
import ar.edu.itba.pod.api.exceptions.*;
import ar.edu.itba.pod.api.utils.Pair;

import java.util.TreeSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FlightsManagement {
    private Map<String, Flight> flightsMap;
    private List<PlaneModel> planeModels;
    private SortedSet<Flight> flightsCancelled;

    private final ReadWriteLock flightsMapLock;
    private final ReadWriteLock planeModelsLock;
    private final ReadWriteLock flightsCancelledLock;
    private final ReadWriteLock notificationLock;
    private final ReadWriteLock clientsToNotifyLock;

    // FORMAT: <flightCode, List<Name>>
    private Map<String,List<String>> clientsToNotify;
    private Map<String,Queue<Pair<Integer, String>>> notifications;


    private static final Logger logger = LoggerFactory.getLogger(Server.class);


    public FlightsManagement() {
        this.flightsMap = new HashMap<>();
        this.planeModels = new ArrayList<>();
        this.flightsMapLock = new ReentrantReadWriteLock();
        this.planeModelsLock = new ReentrantReadWriteLock();
        this.flightsCancelledLock = new ReentrantReadWriteLock();
        this.notificationLock = new ReentrantReadWriteLock();
        this.clientsToNotifyLock = new ReentrantReadWriteLock();
        this.flightsCancelled = new TreeSet<>();
        this.clientsToNotify = new HashMap<>();
        this.notifications = new HashMap<>();
        
    }

    ////////////////////////////////////////////////////
    //             Notification Service               //
    ////////////////////////////////////////////////////

    public void addPassengerToNotify(String n, String fc) throws FlightDoesntExistException, PassengerNotInFlightException, PassengerNotSubscribedException{
        notificationLock.writeLock().lock();
        clientsToNotifyLock.writeLock().lock();
        flightsMapLock.readLock().lock();
        try {
            if(!flightsMap.containsKey(fc))
                throw new FlightDoesntExistException(fc);
            Flight f = flightsMap.get(fc);
            if(!flightsMap.get(fc).passengerInFlight(n))
                throw new PassengerNotInFlightException(n);
            if(!clientsToNotify.containsKey(fc))
                clientsToNotify.put(fc, new ArrayList<>());

            logger.info("ANTES DEL SYNCRONIZED");            
            List<String> clientList = clientsToNotify.get(fc);
            synchronized(clientList){
                
                if(clientsToNotify.get(fc).contains(n)){
                    return;
                }
                clientsToNotify.get(fc).add(n);        
                notifications.put(n,new LinkedList<>());
                addNotification(n,fc,"You are following " + f, 0);
                
                logger.info("Me subscribí a notifications ");
                return;
            }
        
        } finally {
            clientsToNotifyLock.writeLock().unlock();
            flightsMapLock.readLock().unlock();
            notificationLock.writeLock().unlock();
        }
    }
    
    public void addNotification(String n, String fc, String motive, Integer code) throws PassengerNotSubscribedException{
        notificationLock.readLock().lock();
        clientsToNotifyLock.readLock().lock();
        try{
            List<String> clientList = clientsToNotify.get(fc);
            if(clientList == null){
                throw new PassengerNotSubscribedException(n,fc);
            }
            synchronized(clientList){
                if(!clientList.contains(n)){
                    throw new PassengerNotSubscribedException(n,fc);
                }
            }
            synchronized(notifications.get(n)){
                notifications.get(n).add(new Pair<>(code, motive));
            }
        }finally{
            notificationLock.readLock().unlock();
            clientsToNotifyLock.readLock().unlock();
        }
    }

    public void notifyStatusChange(String fc,String status, Integer code) throws PassengerNotSubscribedException, FlightDoesntExistException{
        clientsToNotifyLock.readLock().lock();
        flightsMapLock.readLock().lock();
        try{
            Flight f = flightsMap.get(fc);
            if(f != null){
                synchronized(f){
                    for(String n : clientsToNotify.get(fc)){
                        Seat s = getSeatOfPassenger(fc,n);
                        if(s != null){
                            synchronized(s){
                                addNotification(n, fc, "Your " + f.toString() + " was " + status + " and your seat is " + s, code);
                            }
                        }else{     
                            addNotification(n, fc, "Your " + f.toString() + " was " + status + " with no assigned seat", code);
                        }
                    }
                }
            }
            else{
                throw new FlightDoesntExistException(fc);
            }   
        }finally{
            clientsToNotifyLock.readLock().unlock();
            flightsMapLock.readLock().lock();
        }
    }

    public Pair<Integer, String> notifyPassenger(String n, String fc) throws PassengerNotSubscribedException {
        notificationLock.readLock().lock();
        clientsToNotifyLock.readLock().lock();
        try{
            List<String> clientList = clientsToNotify.get(fc);
            if(clientList == null ){
                throw new PassengerNotSubscribedException(n,fc);
            }
            synchronized(clientList){
                if(!clientList.contains(n)){
                    throw new PassengerNotSubscribedException(n,fc);
                }
            }
            
            synchronized(notifications.get(n)){
                return notifications.get(n).poll();
            }
        }finally{
            notificationLock.readLock().unlock();
            clientsToNotifyLock.readLock().unlock();
        }
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
                try {
					notifyStatusChange(flightCode, "confirmed", 1);
				}catch(PassengerNotSubscribedException e){}

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
                try {
					notifyStatusChange(flightCode, "cancelled", 0);
				}catch(PassengerNotSubscribedException e){}

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


    public List<Flight> getPendingFlightsByDestiny(String flightCode){
        List<Flight> ret = new ArrayList<>();
        flightsMapLock.readLock().lock();
        try{
            String destiny = flightsMap.get(flightCode).getDestinyAirportCode();
            for(Flight f : flightsMap.values()){
                synchronized(f){
                    if(f.getStatus() == FlightStatus.PENDING && f.getDestinyAirportCode().equals(destiny) && f.getCompletion() < 1.0 && !f.getFlightCode().equals(flightCode)){
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

    public void changeTicket(Flight oldFlight, Flight newFlight, String passengerName, SeatCategory newCat) throws TicketNotInFlightException, PassengerNotInFlightException, FlightDoesntExistException{
        clientsToNotifyLock.readLock().lock();
        try {
            synchronized(oldFlight){
                for(Ticket t : oldFlight.getTickets()){
                    synchronized(newFlight){
                        synchronized(t){
                            if (t.getPassengerName().equals(passengerName)){
                                oldFlight.getTickets().remove(t);
                                oldFlight.subtractTicketCount(newCat);
                                newFlight.getTickets().add(new Ticket(newCat, passengerName));
                                newFlight.addTicketCount(newCat);
                                List<String> oldFlightClientsList = clientsToNotify.get(oldFlight.getFlightCode());
                                if(oldFlightClientsList == null){
                                    return;
                                }
                                synchronized(oldFlightClientsList){
                                    if(oldFlightClientsList.contains(passengerName)){
                                        try {
                                            logger.info("Antes del addnotification");
                                            addNotification(passengerName, oldFlight.getFlightCode(),"Your ticket changed to " + newFlight.toString() + " from " + oldFlight.toString(),1);
                                            //oldFlightClientsList.remove(passengerName);

                                        } catch (PassengerNotSubscribedException e) {
                                            logger.info("CATCH");
                                        }
                                    }
                                }
                                return;
                            }
                        }
                    }
                }
                throw new TicketNotInFlightException(oldFlight.getFlightCode(), passengerName);
            }
        } finally {
            clientsToNotifyLock.readLock().unlock();
        }
    }

    public Pair<Integer,Map<String,List<String>>> forceTicketChange() throws TicketNotInFlightException, PassengerNotInFlightException, FlightDoesntExistException {
        Map<String, List<String>> map = new HashMap<>();
        int ticketsChanged = 0;
        flightsCancelledLock.readLock().lock();
        try{
            for(Flight cf : flightsCancelled) {
                synchronized(cf){
                    List<Flight> alternativeFlights = getPendingFlightsByDestiny(cf.getFlightCode());
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


    public void assignNewSeatToPassenger(String flightCode, String passengerName, int row, char column) throws FlightDoesntExistException, FlightIsNotPendingException, PassengerDoesntHaveTicketException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException, SeatDoesntExistException {
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
                            try {
                                addNotification(passengerName,f.getFlightCode(), "Your seat is "+ s + " for " + f, 0);
                            } catch (PassengerNotSubscribedException e) {}
                            return;
                        }
                    }
                }
            }
            throw new SeatDoesntExistException(row, column);
        }
    }

    public void unsitPassenger(String flightCode,String passengerName) throws FlightDoesntExistException, FlightIsNotPendingException, PassengerIsNotSeatedException{
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

    public void resitPassenger(String flightCode, String passengerName, int row, char column) throws FlightDoesntExistException, FlightIsNotPendingException, PassengerDoesntHaveTicketException, PassengerIsAlreadySeatedException, SeatIsTakenException, InvalidSeatCategoryException, PassengerIsNotSeatedException, SeatDoesntExistException {
        if(seatIsOccupied(flightCode, row, column).equals("FREE")){
            Seat s1 = getSeatOfPassenger(flightCode, passengerName);
            if(s1 == null){
                return;
            }
            synchronized(s1){
                unsitPassenger(flightCode, passengerName);
                assignNewSeatToPassenger(flightCode, passengerName, row, column);
                Seat s2 = getSeatOfPassenger(flightCode,passengerName);
                if(s2 == null){
                    return;
                }
                synchronized(s2){
                    try {
                        Flight f = flightsMap.get(flightCode);
                        synchronized(f){
                            addNotification(passengerName, flightCode, "Your seat changed to " + s2.toString() + " from " + s1.toString() + " for " + f, 0);
                        }
                    } catch (PassengerNotSubscribedException e) {}
                }
            }
        } else {
            throw new SeatIsTakenException(flightCode, row, column);
        }
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
                return getPendingFlightsByDestiny(flightCode);
            }
        }finally{
            flightsMapLock.readLock().unlock();
        }
    }

    // TODO: revisar si el passenger tenia un seat y deberia ser unsit
    public void changePassengerFlight(String passengerName, String oldFlightCode, String newFlightCode) throws FlightDoesntExistException, FlightIsNotPendingException, TicketNotInFlightException, FlightIsNotAnAlternativeException, PassengerDoesntHaveTicketException, PassengerNotInFlightException{
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
                    if(oldFlight.getStatus() != FlightStatus.PENDING){
                        throw new FlightIsNotPendingException(oldFlightCode);
                    }
                    if(newFlight.getStatus() != FlightStatus.PENDING){
                        throw new FlightIsNotPendingException(newFlightCode);
                    }
                    SeatCategory cat = null;
                    boolean passengerHasTicket = false;
                    for (Ticket t : oldFlight.getTickets()){
                        synchronized(t){
                            if(t.getPassengerName().equals(passengerName)){
                                passengerHasTicket = true;
                                cat = t.getCategory();
                                break;
                            }
                        }
                    }
                    if(!passengerHasTicket){
                        throw new PassengerDoesntHaveTicketException(passengerName,oldFlightCode); 
                    }
                    List<Flight> alternativeFlights = getPendingFlightsByDestiny(oldFlight.getFlightCode());
                    for(Flight f : alternativeFlights){
                        synchronized(f){
                            if(f.getFlightCode().equals(newFlight.getFlightCode())){
                                if(cat == SeatCategory.BUSINESS && f.getbTickets() > 0){
                                    changeTicket(oldFlight, newFlight, passengerName, cat);
                                    return;
                                } else if(cat != SeatCategory.ECONOMY && f.getEpTickets() > 0){
                                    changeTicket(oldFlight, newFlight, passengerName, SeatCategory.PREMIUM_ECONOMY);
                                    return;
                                } else {
                                    changeTicket(oldFlight, newFlight, passengerName, SeatCategory.ECONOMY);
                                }
                                return;
                            }
                        }
                    }
                    throw new FlightIsNotAnAlternativeException(newFlightCode, passengerName);
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
            return "Business";
        }
        if (SeatCategory.PREMIUM_ECONOMY == cat){
            return "Premium Economy";
        }
        if (SeatCategory.ECONOMY == cat){
            return "Economy";
        }
        return null;
    }

    private Seat getSeatOfPassenger(String flightcode, String passenger)  {
        flightsMapLock.readLock().lock();
        try {
            Flight f = flightsMap.get(flightcode);
            synchronized(f){
                for(Seat s : f.getSeats()){
                    if(s.getPassengerName().equals(passenger)){
                        return s;
                    }
                }
                return null;
            }
        } finally {
            flightsMapLock.readLock().unlock();
        }
    }
}