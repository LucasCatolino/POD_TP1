package ar.edu.itba.pod.client;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


import ar.edu.itba.pod.api.entities.PlaneModel;
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

                           
                            Integer brows = null, bcols = null, eprows = null, epcols = null, erows = null, ecols = null;
                            int added = 0;

                            String l;
                            while((l = reader.readLine()) != null){
                                String[] values = l.split(";");
                                //nombre;CATE1#12#1;CATE2...
                                //service.addPlaneModel(values[0],0,10,1,1,1,1);
                                //models.add(values);

                                //leo a partir de las categorias
                                categoriesLoop:
                                for(int i = 1; i < values.length; i++){
                                    String[] categoryInfo = values[i].split("#");
                                    switch (categoryInfo[0]){
                                        case "BUSINESS":
                                            brows = Integer.parseInt(categoryInfo[1]);
                                            bcols = Integer.parseInt(categoryInfo[2]);
                                            if(brows <= 0 || bcols <= 0) {
                                                System.out.println("Cannot add flight " + values[0] + ".");
                                                break categoriesLoop;
                                            }
                                            break;
                                        case "PREMIUM_ECONOMY":
                                            eprows = Integer.parseInt(categoryInfo[1]);
                                            epcols = Integer.parseInt(categoryInfo[2]);
                                            if(eprows <= 0 || epcols <= 0) {
                                                System.out.println("Cannot add flight " + values[0] + ".");
                                                break categoriesLoop;
                                            }
                                            break;
                                        case "ECONOMY":
                                            erows = Integer.parseInt(categoryInfo[1]);
                                            ecols = Integer.parseInt(categoryInfo[2]);
                                            if(erows <= 0 || ecols <= 0) {
                                                System.out.println("Cannot add flight " + values[0] + ".");
                                                break categoriesLoop;
                                            }
                                            break;
                                    }
 
                                }
                                if(brows == null){
                                    brows = 0;
                                    bcols = 0;
                                }
                                if(eprows == null){
                                    eprows = 0;
                                    epcols = 0;
                                }
                                if(erows == null){
                                    erows = 0;
                                    ecols = 0;
                                }

                                if(brows + eprows + erows == 0){
                                    System.out.println("Cannot add flight " + values[0] + ".");
                                    return;
                                }

                                try{
                                    service.addPlaneModel(values[0], brows, bcols, eprows, epcols, erows, ecols);
                                    added++;
                                } catch(PlaneModelAlreadyExistsException e){
                                    System.out.println(e);
                                }
                            }


                        } catch (IOException e) {
                            throw new RuntimeException("Error reading models File.");
                        }
                    }
                case "flights":
                case "status":
                    if(cl.hasOption("Dflight")){
                        FlightStatus fs = service.checkFlightStatus(cl.getOptionValue("Dflight"));
                        System.out.println(status(fs));
                    }
                case "confirm":
                    if(cl.hasOption("Dflight")){
                        service.confirmFlight(cl.getOptionValue("Dflight"));
                    }
                case "cancel":
                    if(cl.hasOption("Dflight")){
                        service.cancelFlight(cl.getOptionValue("Dflight"));
                    }
                case "reticketing":
                    service.forceTicketChange();
            }
        } catch (ParseException e) {
            System.out.print("Parse error: ");
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


}

// if(cl.hasOption("DserverAddress")){
//     String server = cl.getOptionValue("DserverAddress");
//     FlightManagementService service = (FlightManagementService) Naming.lookup(String.format("//%s/%s", server, FlightManagementService.class.getName()));
//     service.addPlaneModel("Boeing 787", 2, 3, 3, 3, 20, 10);
//     SortedSet<Ticket> s = new TreeSet<>();
//     s.add(new Ticket(SeatCategory.ECONOMY, "Joaco"));
//     s.add(new Ticket(SeatCategory.BUSINESS, "Nava"));
//     service.addFlight("Boeing 787", "AA100", "JFK", s);
//     System.out.println(cl.getOptionValue("DserverAddress"));
//     if(cl.hasOption("Daction")){
//         String a = cl.getOptionValue("Daction");
//         if(a.equals("status")){
//             if(cl.hasOption("Dflight")){
//                 String fc = cl.getOptionValue("Dflight");
//                 FlightStatus fs = service.checkFlightStatus(fc);
//                 System.out.println(status(fs));
//             }
//         }
//     }
// }
