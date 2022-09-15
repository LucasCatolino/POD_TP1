package ar.edu.itba.pod.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


import ar.edu.itba.pod.api.exceptions.*;
import ar.edu.itba.pod.api.services.FlightNotificationService;
import ar.edu.itba.pod.api.utils.Pair;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class FlightNotificationClient {
    // $> ./run-notifcations -DserverAddress=xx.xx.xx.xx:yyyy -Dflight=flightCode -Dpassenger=name
    public static void main(String[] args) {
        Options options = new Options();
        Option addr = Option.builder("DserverAddress")
            .valueSeparator('=')
            .required(true)
            .desc("ServerAddress")
            .longOpt("DserverAddress")
            .hasArg(true)
            .build();
        Option flight = Option.builder("Dflight")
            .valueSeparator('=')
            .required(true)
            .desc("Flight")
            .longOpt("Dflight")
            .hasArg(true)
            .build();
        Option passenger = Option.builder("Dpassenger")
            .valueSeparator('=')
            .required(true)
            .desc("Passenger")
            .longOpt("Dpassenger")
            .hasArg(true)
            .build();
        options.addOption(addr);
        options.addOption(flight);
        options.addOption(passenger);
        CommandLine cl;
        CommandLineParser clp = new DefaultParser();
        try {
            cl = clp.parse(options, args);
            String server = cl.getOptionValue("DserverAddress");
            FlightNotificationService service = (FlightNotificationService) Naming.lookup(String.format("//%s/%s", server,FlightNotificationService.class.getName()));
            boolean notConfirmed = true;
            service.registerPassengerToNotify(cl.getOptionValue("Dflight"), cl.getOptionValue("Dpassenger"));
            
            while(notConfirmed){
                Thread.sleep(1000);
                Pair<Integer, String> notification = service.notify(cl.getOptionValue("Dflight"), cl.getOptionValue("Dpassenger"));
                if(notification != null){
                    System.out.println(notification.second);
                    if(notification.first == 1){
                        notConfirmed = false;
                    }
                }             
                }

        } catch( ParseException |
        RemoteException | FlightDoesntExistException | PassengerNotInFlightException
        | PassengerNotSubscribedException | InterruptedException | MalformedURLException | NotBoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
