package ar.edu.itba.pod.client;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

import ar.edu.itba.pod.api.utils.Pair;
import ar.edu.itba.pod.api.entities.*;
import ar.edu.itba.pod.api.exceptions.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ar.edu.itba.pod.api.entities.FlightStatus;
import ar.edu.itba.pod.api.services.FlightManagementService;

public class FlightManagementClient {
    // $> ./run-admin -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName [ -DinPath=filename | -Dflight=flightCode ]

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
        
        Options options = new Options();
        Option addr = Option.builder("DserverAddress")
            .valueSeparator('=')
            .required(true)
            .desc("ServerAddress")
            .longOpt("DserverAddress")
            .hasArg(true)
            .build();
        Option action = Option.builder("Daction")
            .valueSeparator('=')
            .required(true)
            .desc("Action")
            .longOpt("Daction")
            .hasArg(true)
            .build();
        Option inPath = Option.builder("DinPath")
            .valueSeparator('=')
            .desc("inPath")
            .longOpt("DinPath")
            .hasArg(true)
            .build();
        Option flight = Option.builder("Dflight")
            .valueSeparator('=')
            .desc("Flight")
            .longOpt("Dflight")
            .hasArg(true)
            .build();
        options.addOption(addr);
        options.addOption(action);
        options.addOption(inPath);
        options.addOption(flight);
        CommandLine cl;
        CommandLineParser clp = new DefaultParser();
        try {
            cl = clp.parse(options, args);
            String actionSelected = cl.getOptionValue("Daction");
            String server = cl.getOptionValue("DserverAddress");
            FlightManagementService service = (FlightManagementService) Naming.lookup(String.format("//%s/%s", server, FlightManagementService.class.getName()));
            switch(actionSelected){
                //TODO: hacer manejo de CSV para estos dos parametros
                case "model":
                    if(cl.hasOption("DinPath")){
                        try {
                            
                            BufferedReader reader = new BufferedReader(new FileReader(cl.getOptionValue("DinPath")));
                            
                            reader.readLine();
                            int brows = 0, bcols = 0, eprows = 0, epcols = 0, erows = 0, ecols = 0;
                            int added = 0;

                            String l;
                            int flag;
                            while((l = reader.readLine()) != null){
                                flag = 0;
                                //AFK;BUSI#1#2,,ECO#3#2
                                String[] values = l.split(";");
                                String[] cats = values[1].split(",");
                                //leo a partir de las categorias
                                categoriesLoop:
                                for(String c : cats){
                                    String[] categoryInfo = c.split("#");
                                    switch (categoryInfo[0]){
                                        case "BUSINESS":
                                            brows = Integer.parseInt(categoryInfo[1]);
                                            bcols = Integer.parseInt(categoryInfo[2]);
                                            if(brows <= 0 || bcols <= 0) {
                                                flag = 1;
                                                break categoriesLoop;
                                            }
                                            break;
                                        case "PREMIUM_ECONOMY":
                                            eprows = Integer.parseInt(categoryInfo[1]);
                                            epcols = Integer.parseInt(categoryInfo[2]);
                                            if(eprows <= 0 || epcols <= 0) {
                                                flag = 1;
                                                break categoriesLoop;
                                            }
                                            break;
                                        case "ECONOMY":
                                            erows = Integer.parseInt(categoryInfo[1]);
                                            ecols = Integer.parseInt(categoryInfo[2]);
                                            if(erows <= 0 || ecols <= 0) {
                                                flag = 1;
                                                break categoriesLoop;
                                            }
                                            break;


                                    }
                                }
                                /*for(int i = 1; i < values.length; i++){
                                    String[] categoryInfo = values[i].split("#");
                                    switch (categoryInfo[0]){
                                        case "BUSINESS":
                                            brows = Integer.parseInt(categoryInfo[1]);
                                            bcols = Integer.parseInt(categoryInfo[2]);
                                            if(brows <= 0 || bcols <= 0) {
                                                flag = 1;
                                                break categoriesLoop;
                                            }
                                            break;
                                        case "PREMIUM_ECONOMY":
                                            eprows = Integer.parseInt(categoryInfo[1]);
                                            epcols = Integer.parseInt(categoryInfo[2]);
                                            if(eprows <= 0 || epcols <= 0) {
                                                flag = 1;
                                                break categoriesLoop;
                                            }
                                            break;
                                        case "ECONOMY":
                                            erows = Integer.parseInt(categoryInfo[1]);
                                            ecols = Integer.parseInt(categoryInfo[2]);
                                            if(erows <= 0 || ecols <= 0) {
                                                flag = 1;
                                                break categoriesLoop;
                                            }
                                            break;
                                    }
                                }*/
                                if(flag == 1){
                                    System.out.println("Cannot add model " + values[0] + ".");
                                } else {
                                    try{
                                        service.addPlaneModel(values[0], brows, bcols, eprows, epcols, erows, ecols);
                                        added++;
                                    } catch(PlaneModelAlreadyExistsException e){
                                        System.out.println("Cannot add model " + values[0] + ".");
                                    }
                                }
                            }
                            System.out.println("Added " + added + " models");
                        } catch (IOException e) {
                            //throw new RuntimeException("Error reading models File.");
                            //System.out.println(e.getMessage());                
                        }
                    }
                    break;
                case "flights":
                //Boeing 787;AA100;JFK;BUSINESS#John,ECONOMY#Juliet,BUSINESS#Elizabeth
                //Plane Model; Flight Code; Destiny; SeatCategory#PassengerName, SeatCategory#PassengerName....
                    if(cl.hasOption("DinPath")){
                        try{
                            BufferedReader reader = new BufferedReader(new FileReader(cl.getOptionValue("DinPath")));
                            reader.readLine();
                            int added = 0;
                            String line;
                            while((line = reader.readLine()) != null){
                                
                                String[] values = line.split(";");
                                String[] tickets = values[3].split(",");
                                
                                SortedSet<Ticket> ticketsList = new TreeSet<Ticket>();
                                for(String t : tickets){
                                    String[] ticketArgs = t.split("#"); 
                                    ticketsList.add(new Ticket(getCategory(ticketArgs[0]),ticketArgs[1]));
                                }
                                try {
                                    service.addFlight(values[0],values[1],values[2],ticketsList); 
                                    added++;
                                } catch (FlightAlreadyExistsException | PlaneModelDoesntExistsException e) {
                                    System.out.println("Cannot add flight " + values[1]);
                                } 
                            }
                            System.out.println(added + " flights added");
                        }catch(IOException e){
                            e.printStackTrace();
                        } 
                    }
                case "status":
                    if(cl.hasOption("Dflight")){
                        FlightStatus fs = service.checkFlightStatus(cl.getOptionValue("Dflight"));
                        System.out.println("Flight " + cl.getOptionValue("Dflight") + " is " + status(fs));
                    }
                    break;
                case "confirm":
                    if(cl.hasOption("Dflight")){
                        service.confirmFlight(cl.getOptionValue("Dflight"));
                        System.out.println("Flight " + cl.getOptionValue("Dflight") + "was confirmed");
                    }
                    break;
                case "cancel":
                    if(cl.hasOption("Dflight")){
                        service.cancelFlight(cl.getOptionValue("Dflight"));
                        System.out.println("Flight " + cl.getOptionValue("Dflight") + "was cancelled");
                    }
                    break;
                case "reticketing":
                    Pair<Integer, Map<String, List<String>>> ret = service.forceTicketChange();
                    System.out.println(ret.first + " tickets changed");
                    for(String fc : ret.second.keySet()){
                        for(String pn : ret.second.get(fc)){
                            System.out.println("Cannot find alternative flight for " + pn + " with Ticket " + fc);
                        }
                    }
                    break;
            }
        } catch (ParseException | FlightDoesntExistException | TicketNotInFlightException   e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static String status(FlightStatus status){
        if(FlightStatus.PENDING == status){
            return new String("Pending");
        }
        if (FlightStatus.CONFIRMED == status){
            return new String("Confirmed");
        }
        if (FlightStatus.CANCELED == status){
            return new String("Cancelled");
        }
        return "Unknown";
    }
    
    public static SeatCategory getCategory(String category) throws IOException {
        if(category.equals("BUSINESS")){
            return SeatCategory.BUSINESS;
        }
        if(category.equals("PREMIUM_ECONOMY")){
            return SeatCategory.PREMIUM_ECONOMY;
        }
        if(category.equals("ECONOMY")){
            return SeatCategory.ECONOMY;
        }
        throw new IOException();
    }
}
