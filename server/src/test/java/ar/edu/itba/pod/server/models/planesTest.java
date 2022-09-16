package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.api.entities.*;
import ar.edu.itba.pod.api.exceptions.FlightAlreadyExistsException;
import ar.edu.itba.pod.api.exceptions.FlightDoesntExistException;

import ar.edu.itba.pod.api.exceptions.PlaneModelAlreadyExistsException;
import ar.edu.itba.pod.api.exceptions.PlaneModelDoesntExistsException;

import ar.edu.itba.pod.server.FlightsManagement;
import static org.junit.Assert.*;


import org.junit.Test;



import java.util.*;
import java.util.concurrent.*;

public class planesTest {
    private static final String MODEL_NAME_1 = "AFK1";
    private static final String MODEL_NAME_2 = "AFK2";
    private static final String MODEL_NAME_3 = "AFK3";

    private static final int B_ROWS = 4;
    private static final int B_COLS = 4;
    private static final int EP_ROWS = 5;
    private static final int EP_COLS = 5;
    private static final int E_ROWS = 6;
    private static final int E_COLS = 6;

    private static final int VUELITOS_ = 100;

    private static final String DESTINY_1 = "JFK";
    private static final String DESTINY_2 = "AEP";
    private static final String DESTINY_3 = "LAG";

    private static final String CODE_1 = "AA100";
    private static final String CODE_2 = "AA101";
    private static final String CODE_3 = "AA102";

    private static final String PASSENGER_1 = "SANTI";
    private static final String PASSENGER_2 = "NAVA";
    private static final String PASSENGER_3 = "GER";
    


    private static final Ticket T1 = new Ticket(SeatCategory.BUSINESS, PASSENGER_1);
    private static final Ticket T2 = new Ticket(SeatCategory.PREMIUM_ECONOMY, PASSENGER_2);
    private static final Ticket T3 = new Ticket(SeatCategory.ECONOMY, PASSENGER_3);

    private static final int THREAD_COUNT = 5;

    private static final FlightsManagement fm = new FlightsManagement();



    private final Runnable modelCreator = () ->{

        try {
            fm.createNewPlaneModel(MODEL_NAME_1, B_ROWS, B_COLS, EP_ROWS, EP_COLS, E_ROWS, E_COLS);
        } catch (PlaneModelAlreadyExistsException e) {
            System.out.println(e.getMessage());
        }
        try {
            fm.createNewPlaneModel(MODEL_NAME_2, B_ROWS, B_COLS, 0, 0, E_ROWS, E_COLS);
        } catch (PlaneModelAlreadyExistsException e) {
            System.out.println(e.getMessage());
        }
        try {
            fm.createNewPlaneModel(MODEL_NAME_3, B_ROWS, B_COLS, EP_ROWS, EP_COLS, 0, 0);
        } catch (PlaneModelAlreadyExistsException e) {
            System.out.println(e.getMessage());
        }
        for(int i = 0; i < VUELITOS_; i++){
            String model = "CARLA_" + i;
            try {
                fm.createNewPlaneModel(model, B_ROWS, B_COLS, EP_ROWS, EP_COLS, E_ROWS, E_COLS);
            } catch (PlaneModelAlreadyExistsException e) {
                System.out.println(e.getMessage());
            }

        }


    };

    @Test
    public void testCreateNewPlaneModel() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for(int i = 0; i < THREAD_COUNT; i++){
            final Thread thread = new Thread(modelCreator);
            thread.start();
            threads.add(thread);
        }
        for(Thread t : threads){
            t.join();
        }
        assertEquals(103, fm.getPlaneModels().size());
    }

    private Runnable flightCreator = () -> {
        SortedSet<Ticket> tickets = new TreeSet<>();
        tickets.add(T1);
        tickets.add(T2);
        tickets.add(T3);
        try {
            fm.createNewFlight(MODEL_NAME_1, CODE_1, DESTINY_1, tickets);
        } catch (PlaneModelDoesntExistsException | FlightAlreadyExistsException e) {
            System.out.println(e.getMessage());
        }
            try {
                fm.createNewFlight(MODEL_NAME_2, CODE_2, DESTINY_2, tickets);
            } catch (PlaneModelDoesntExistsException | FlightAlreadyExistsException e) {
                System.out.println(e.getMessage());
            }
            try {
                fm.createNewFlight(MODEL_NAME_3, CODE_3, DESTINY_3, tickets);
            } catch (PlaneModelDoesntExistsException | FlightAlreadyExistsException e) {
                System.out.println(e.getMessage());
            }
            for(int i = 0; i < VUELITOS_; i++){
                String c = "VUELITO_" + i;
                try {
                    fm.createNewFlight(MODEL_NAME_3, c, DESTINY_3, tickets);
                } catch (PlaneModelDoesntExistsException | FlightAlreadyExistsException e) {
                    System.out.println(e.getMessage());
                }
            }
        };



    @Test
    public void testCreateNewFlight() throws InterruptedException, FlightDoesntExistException, ExecutionException {
        Collection<Callable<Object>> callables = new ArrayList<>();
        ExecutorService pool = Executors.newCachedThreadPool();
        for(int i = 0; i < THREAD_COUNT; i++){
            callables.add(Executors.callable(flightCreator));
        }
        pool.invokeAll(callables);
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(3+VUELITOS_, fm.getFlightsMap().keySet().size());
    }
}
