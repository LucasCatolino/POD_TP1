package ar.edu.itba.pod.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import ar.edu.itba.pod.api.exceptions.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ar.edu.itba.pod.api.entities.*;
import ar.edu.itba.pod.api.services.SeatMapConsultationService;

public class SeatMapConsultationClient {
    // $> ./run-seatMap -DserverAddress=xx.xx.xx.xx:yyyy -Dflight=flightCode [ -Dcategory=catName | -Drow=rowNumber ] -DoutPath=output.csv

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
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
        Option category = Option.builder("Dcategory")
            .valueSeparator('=')
            .desc("Category")
            .longOpt("Dcategory")
            .hasArg(true)
            .build();
        Option row = Option.builder("Drow")
            .valueSeparator('=')
            .desc("Row")
            .longOpt("Drow")
            .hasArg(true)
            .build();
        Option outPath = Option.builder("DoutPath")
            .valueSeparator('=')
            .desc("OutPath")
            .longOpt("DoutPath")
            .hasArg(true)
            .build();
        options.addOption(addr);
        options.addOption(flight);
        options.addOption(category);
        options.addOption(row);
        options.addOption(outPath);
        CommandLine cl;
        CommandLineParser clp = new DefaultParser();
        try {
            //TODO: HACER QUE CARGUE UN CSV
            cl = clp.parse(options, args);
            String server = cl.getOptionValue("DserverAddress");
            SeatMapConsultationService service = (SeatMapConsultationService) Naming.lookup(String.format("//%s/%s", server, SeatMapConsultationService.class.getName()));
            // TODO: ACA hay que recibir la info en listas y de ahi llenar los output.csv
            // TODO: En caso de error o rta vacia no llenar archivos, solo imprimir por pantalla
            if(cl.hasOption("Dcategory")){
                SeatCategory cat = getCategory(cl.getOptionValue("Dcategory"));
                if(cat != null){
                    List<Seat> ret = service.consultSeatMap(cl.getOptionValue("Dflight"), cat);
                    printSeatMap(ret);
                }
            }
            else if (cl.hasOption("Drow")){
                List<Seat> ret = service.consultSeatMap(cl.getOptionValue("Dflight"), Integer.parseInt(cl.getOptionValue("Drow")));
                printSeatMap(ret);
            }
            else {
                List<Seat> ret = service.consultSeatMap(cl.getOptionValue("Dflight"));
                printSeatMap(ret);
            }
        } catch (ParseException e) {
            System.out.print("Parse error: ");
            System.out.println(e.getMessage());
        } catch (SeatRowDoesntExistException e){
            System.out.println(e.getMessage());
        } catch (FlightDoesntExistException e){
            System.out.println(e.getMessage());
        } catch (SeatCategoryDoesntExistException e){
            System.out.println(e.getMessage());
        }
    }

    private static SeatCategory getCategory(String cat){
        if(cat.equals("BUSINESS")){
            return SeatCategory.BUSINESS;
        }
        if(cat.equals("PREMIUN_ECONOMY")){
            return SeatCategory.PREMIUM_ECONOMY;
        }
        if(cat.equals("ECONOMY")){
            return SeatCategory.ECONOMY;
        }
        return null;
    }

    private static void printSeatMap(List<Seat> seats){
        seats.sort((s1,s2) -> {
            if(s1.getRow() == s2.getRow()){
                return Character.compare(s1.getColumn(), s2.getColumn());
            } 
            return s1.getRow() - s2.getRow();
        });
        StringBuilder ret = new StringBuilder();
        ret.append(" | ");
        SeatCategory lastCat = null;
        int row = seats.get(0).getRow();
        for(Seat s : seats){
            if(s.getRow() != row){
                row++;
                ret.append('\t').append(lastCat).append('\n').append(" | ");
            }
            ret.append(s.getRow()).append(" ").append(s.getColumn()).append(" ");
            if(s.getPassengerName().equals("FREE")){
                ret.append("*");
            } else {
                ret.append(s.getPassengerName().charAt(0));
            }
            ret.append(" ").append(" | ");
            lastCat = s.getCategory();
        }
        ret.append('\t').append(lastCat).append('\n');
        System.out.println(ret);
    }
}
