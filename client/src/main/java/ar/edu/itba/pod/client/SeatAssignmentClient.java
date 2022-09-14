package ar.edu.itba.pod.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import ar.edu.itba.pod.api.exceptions.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ar.edu.itba.pod.api.entities.*;
import ar.edu.itba.pod.api.services.SeatAssignmentService;

public class SeatAssignmentClient {
    // $> ./run-seatAssign -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName -Dflight=flightCode [ -Dpassenger=name | -Drow=num | -Dcol=L | -DoriginalFlight=originFlightCode ]

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
        Option flight = Option.builder("Dflight")
            .valueSeparator('=')
            .required(true)
            .desc("Flight")
            .longOpt("Dflight")
            .hasArg(true)
            .build();
        Option passenger = Option.builder("Dpassenger")
            .valueSeparator('=')
            .desc("Passenger")
            .longOpt("Dpassenger")
            .hasArg(true)
            .build();
        Option row = Option.builder("Drow")
            .valueSeparator('=')
            .desc("Row")
            .longOpt("Drow")
            .hasArg(true)
            .build();
        Option col = Option.builder("Dcol")
            .valueSeparator('=')
            .desc("Col")
            .longOpt("Dcol")
            .hasArg(true)
            .build();
        Option originalFlight = Option.builder("DoriginalFlight")
            .valueSeparator('=')
            .desc("originalFlight")
            .longOpt("DoriginalFlight")
            .hasArg(true)
            .build();
        options.addOption(addr);
        options.addOption(action);
        options.addOption(flight);
        options.addOption(passenger);
        options.addOption(row);
        options.addOption(col);
        options.addOption(originalFlight);
        CommandLine cl;
        CommandLineParser clp = new DefaultParser();
        try {
            cl = clp.parse(options, args);
            String actionSelected = cl.getOptionValue("Daction");
            String server = cl.getOptionValue("DserverAddress");
            SeatAssignmentService service = (SeatAssignmentService) Naming.lookup(String.format("//%s/%s", server, SeatAssignmentService.class.getName()));
            switch(actionSelected){
                case "status":
                    if(cl.hasOption("Drow") && cl.hasOption("Dcol")){
                        //TODO: revisar el ultimo parametro y si hay que verificar que ingresen algo valido
                        String ret = service.seatIsOccupied(cl.getOptionValue("Dflight"), Integer.parseInt(cl.getOptionValue("Drow")), (cl.getOptionValue("Dcol").charAt(0)));
                        if(ret.equals("FREE")){
                            System.out.println("Seat " + cl.getOptionValue("Drow") +  cl.getOptionValue("Dcol") + " is " + ret );
                        }else{
                            System.out.println("Seat " + cl.getOptionValue("Drow") +  cl.getOptionValue("Dcol") + " is ASSIGNED to " + ret );
                        }
                    }
                case "assign":
                    if(cl.hasOption("Drow") && cl.hasOption("Dcol") && cl.hasOption("Dpassenger")){
                        service.assignNewSeatToPassenger(cl.getOptionValue("Dflight"), cl.getOptionValue("Dpassenger"), Integer.parseInt(cl.getOptionValue("Drow")), cl.getOptionValue("Dcol").charAt(0));
                    }
                case "move":
                    if(cl.hasOption("Drow") && cl.hasOption("Dcol") && cl.hasOption("Dpassenger")){
                        service.movePassengerToNewSeat(cl.getOptionValue("Dflight"), cl.getOptionValue("Dpassenger"), Integer.parseInt(cl.getOptionValue("Drow")), cl.getOptionValue("Dcol").charAt(0));
                    }
                case "alternatives":
                    if(cl.hasOption("Dpassenger")){
                        //TODO: ver en la consigna como imprimir esto (no tenemos el destiny y se necesita, asique deberiamos hacer un get a la api)
                        List<Flight> list = service.listAlternativeFlightSeats(cl.getOptionValue("Dflight"), cl.getOptionValue("Dpassenger"));
                        printAlternativeSeats(list);
                    }
                case "changeTicket":
                    if(cl.hasOption("Dpassenger") && cl.hasOption("DoriginalFlight")){
                        service.changePassengerFlight(cl.getOptionValue("Dpassenger"), cl.getOptionValue("DoriginalFlight"), cl.getOptionValue("Dflight"));
                    }
            }
            //TODO: VER QUE ONDA ESTOS CATCHs
        } catch (ParseException e) {
            System.out.print("Parse error: ");
            System.out.println(e.getMessage());
        } catch (PassengerIsNotSeatedException e) {
            e.printStackTrace();
        } catch (InvalidSeatCategoryException e) {
            e.printStackTrace();
        } catch (PassengerIsAlreadySeatedException e) {
            e.printStackTrace();
        } catch (TicketNotInFlightException e) {
            e.printStackTrace();
        } catch (FlightDoesntExistException e) {
            e.printStackTrace();
        } catch (SeatIsTakenException e) {
            e.printStackTrace();
        } catch (FlightIsNotAnAlternativeException e) {
            e.printStackTrace();
        } catch (SeatDoesntExistException e) {
            e.printStackTrace();
        } catch (PassengerDoesntHaveTicketException e) {
            e.printStackTrace();
        } catch (FlightIsNotPendingException e) {
            e.printStackTrace();
        }
    }


    private static Integer getFreeSeats(Flight f, SeatCategory cat){
        if(cat == SeatCategory.BUSINESS){
            return f.getbTickets();
        }
        if(cat == SeatCategory.PREMIUM_ECONOMY){
            return f.getEpTickets();
        }
        return f.geteTickets();
    }
    
    private static void sortFlightsByFreeSeats(List<Flight> flights, SeatCategory cat){
        flights.sort((f1,f2) -> {
            if(getFreeSeats(f1,cat).equals(getFreeSeats(f2, cat))){
                return f1.getFlightCode().compareTo(f2.getFlightCode());
            }
            return getFreeSeats(f2,cat).compareTo(getFreeSeats(f1, cat));
            });
        }

    private static void printFreeSeats(List<Flight> flights, SeatCategory cat){
        sortFlightsByFreeSeats(flights, cat);
        for(Flight f : flights){
            if(getFreeSeats(f,cat) != 0){
                System.out.println(f.getDestinyAirportCode() + " | " + f.getFlightCode() + " | " + getFreeSeats(f, cat) + " " + getCategoryString(cat));
            }
            else
                break;
        }
    }

    private static void printAlternativeSeats(List<Flight> flights){
        printFreeSeats(flights,SeatCategory.BUSINESS);
        printFreeSeats(flights,SeatCategory.PREMIUM_ECONOMY);
        printFreeSeats(flights,SeatCategory.ECONOMY);
    }


    public static String getCategoryString(SeatCategory cat){
        if(SeatCategory.BUSINESS == cat){
            return new String("BUSINESS");
        }if(SeatCategory.PREMIUM_ECONOMY == cat){
            return new String("PREMIUM_ECONOMY");
        }
        if (SeatCategory.ECONOMY == cat){
            return new String("ECONOMY");
        }
        return null;
    }
}