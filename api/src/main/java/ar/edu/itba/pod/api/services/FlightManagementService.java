package ar.edu.itba.pod.api.services;


import ar.edu.itba.pod.api.entities.FlightStatus;
import ar.edu.itba.pod.api.entities.SeatCategory;
import ar.edu.itba.pod.api.entities.Ticket;

import java.rmi.Remote;
import java.util.List;
import java.util.Map;

public interface FlightManagementService extends Remote {

    void addPlaneModel(String modelName,
                        int businessRows,int businessCols,
                        int epRows, int epCols,
                        int econRows, int econCols);
                        
    void addFlight(String planeModelName, String flightCode, String destinyAirportCode, List<Ticket> tickets);

    FlightStatus checkFlightStatus(String flightCode);

    void confirmFlight(String flightCode);

    void cancelFlight(String flightCode);

    void forceTicketChange();
}
