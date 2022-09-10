package ar.edu.itba.pod.server;

import ar.edu.itba.pod.api.entities.Flight;
import ar.edu.itba.pod.api.entities.PlaneModel;
import ar.edu.itba.pod.api.exceptions.PlaneModelAlreadyExistsException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FlightsManagement {
    Map<String, Flight> flightsMap;
    List<PlaneModel> planeModels;

    private final ReadWriteLock flightsMapLock;


    public FlightsManagement() {
        this.flightsMap = new HashMap<>();
        flightsMapLock = new ReentrantReadWriteLock();
    }

    public void createNewPlaneModel(String name, int brows, int bcols, int eprows, int epcols, int erows, int ecols) throws PlaneModelAlreadyExistsException {
        PlaneModel planeModelToAdd = new PlaneModel(name,brows,bcols,eprows,epcols,erows,ecols);
        synchronized(planeModels){
            if(planeModels.contains(planeModelToAdd)){
                throw new PlaneModelAlreadyExistsException(planeModelToAdd.getName());
            }
            planeModels.add(planeModelToAdd);
        }
    }

    public Flight findFlightByCode(String flightCode){
        flightsMapLock.readLock().lock();
        try {
            return flightsMap.get(flightCode);
        }finally {
            flightsMapLock.readLock().unlock();
        }
    }
}
