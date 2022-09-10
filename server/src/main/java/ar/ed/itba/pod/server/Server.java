package ar.ed.itba.pod.server;

import ar.edu.itba.pod.api.services.FlightManagementService;
import ar.edu.itba.pod.api.services.FlightNotificationService;
import ar.edu.itba.pod.api.services.SeatAssignmentService;
import ar.edu.itba.pod.api.services.SeatMapConsultationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws RemoteException {
        logger.info("POD_TP1 Server Starting ...");
        // Create Service and Export it
        final Servant servant = new Servant();
        final Remote remote = UnicastRemoteObject.exportObject(servant, 0);

        // Bind Services in registry name
        final Registry registry = LocateRegistry.getRegistry("localhost");
        registry.rebind(FlightManagementService.class.getName(), remote);
        registry.rebind(FlightNotificationService.class.getName(), remote);
        registry.rebind(SeatAssignmentService.class.getName(), remote);
        registry.rebind(SeatMapConsultationService.class.getName(), remote);

        logger.info("Server online");

    }
}
