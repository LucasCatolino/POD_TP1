package ar.edu.itba.pod.client;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


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
