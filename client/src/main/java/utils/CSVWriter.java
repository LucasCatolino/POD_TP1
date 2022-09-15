package utils;

import ar.edu.itba.pod.api.entities.Seat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVWriter {

    public void writeToCSV(List<Seat> seats, String outFile) throws FileNotFoundException {
        List<String[]> dataLines = new ArrayList<>();

        for(Seat s : seats){
            if(s.getPassengerName().equals("FREE")){
                dataLines.add(new String[]
                        {Integer.toString(s.getRow()), String.valueOf(s.getColumn()), "*", s.getCategory().toString()});
            }else
                dataLines.add(new String[]
                        {Integer.toString(s.getRow()), String.valueOf(s.getColumn()), String.valueOf(s.getPassengerName().charAt(0)), s.getCategory().toString()});
        }
        File csvOutputFile = new File(outFile);
        try (PrintWriter pw = new PrintWriter(outFile)) {
            dataLines.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }


    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .collect(Collectors.joining(";"));
    }
}
