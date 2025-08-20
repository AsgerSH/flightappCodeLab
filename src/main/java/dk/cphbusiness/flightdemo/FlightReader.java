package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.flightdemo.dtos.AirlineDTO;
import dk.cphbusiness.flightdemo.dtos.ArrivalDTO;
import dk.cphbusiness.flightdemo.dtos.FlightDTO;
import dk.cphbusiness.flightdemo.dtos.FlightInfoDTO;
import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {

    public static void main(String[] args) {
        try {
            List<FlightDTO> flightList = getFlightsFromFile("flights.json");
            List<FlightInfoDTO> flightInfoDTOList = getFlightInfoDetails(flightList);

            //All flight informationer
            //flightInfoDTOList.forEach(System.out::println);

            // Tjek gennemsnitstid for airline
//            System.out.println("Avg: " + averageFlightTimeForAirline(flightInfoDTOList, "Lufthansa"));

            // Alle flyture fra én airline
//            System.out.println(totalFlightForAirline(flightList));

            // Alle flyture mellem to lufthavne
//            List<FlightInfoDTO> flightsBetween = listOfFlightsBetweenAirports(flightInfoDTOList, "Fukuoka", "Haneda Airport");
//            flightsBetween.forEach(System.out::println);

            // Alle flyture før et givent tidspunkt på døgnet
//            List<FlightInfoDTO> flightsBefore = listOfFlightsBeforeSpecificTime(flightInfoDTOList, LocalTime.of(1, 0));
//            flightsBefore.forEach(System.out::println);

            // Alle flyture sorteret på arrival time
//            List<FlightInfoDTO> flightsSorted = listOfFlightsSortedByArrival(flightInfoDTOList);
//            flightsSorted.forEach(System.out::println);

            // Totalsummen af alle airlines flytid
//            Map<String, Long> totalTimes = totalFlightTimePerAirline(flightInfoDTOList);
//            totalTimes.forEach((airline, minutes) ->
//                    System.out.println(airline + " total flight time: " + (minutes / 60) + "h " + (minutes % 60) + "m"));

            // Alle flyture sorteret på duration
            List<FlightInfoDTO> flightsSorted = listOfAllFlightsSortedByDuration(flightInfoDTOList);
            flightsSorted.forEach(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FlightDTO> getFlightsFromFile(String filename) throws IOException {

        ObjectMapper objectMapper = Utils.getObjectMapper();

        // Deserialize JSON from a file into FlightDTO[]
        FlightDTO[] flightsArray = objectMapper.readValue(Paths.get("flights.json").toFile(), FlightDTO[].class);

        // Convert to a list
        List<FlightDTO> flightsList = List.of(flightsArray);
        return flightsList;
    }

    public static List<FlightInfoDTO> getFlightInfoDetails(List<FlightDTO> flightList) {
        List<FlightInfoDTO> flightInfoList = flightList.stream()
                .map(flight -> {
                    LocalDateTime departure = flight.getDeparture().getScheduled();
                    LocalDateTime arrival = flight.getArrival().getScheduled();
                    Duration duration = Duration.between(departure, arrival);
                    FlightInfoDTO flightInfo =
                            FlightInfoDTO.builder()
                                    .name(flight.getFlight().getNumber())
                                    .iata(flight.getFlight().getIata())
                                    .airline(flight.getAirline().getName())
                                    .duration(duration)
                                    .departure(departure)
                                    .arrival(arrival)
                                    .origin(flight.getDeparture().getAirport())
                                    .destination(flight.getArrival().getAirport())
                                    .build();

                    return flightInfo;
                })
                .toList();
        return flightInfoList;
    }

    // Opgave 1
    public static double averageFlightTimeForAirline(List<FlightInfoDTO> flightInfoDTOList, String airline) {
        return flightInfoDTOList.stream()
                .filter(f -> f.getAirline() != null)
                .filter(f -> f.getAirline().equalsIgnoreCase(airline))
                .mapToLong(f -> f.getDuration().toMinutes())
                .average().orElse(0.0);
    }

    // Opgave 2 Add a new feature (make a list of flights that are operated between two specific airports.
    // For example, all flights between Fukuoka and Haneda Airport)

    public static List<FlightInfoDTO> listOfFlightsBetweenAirports(List<FlightInfoDTO> flightInfoList, String airport1, String airport2) {
        return flightInfoList.stream()
                .filter(f -> f.getOrigin() != null && f.getDestination() != null)
                .filter(f ->
                        (f.getOrigin().equalsIgnoreCase(airport1) && f.getDestination().equalsIgnoreCase(airport2)) ||
                                (f.getOrigin().equalsIgnoreCase(airport2) && f.getDestination().equalsIgnoreCase(airport1))
                )
                .toList();
    }
    // Opgave 3 Add a new feature
    // (make a list of flights that leaves before a specific time in the day/night. For example, all flights that leave before 01:00)

    public static List<FlightInfoDTO> listOfFlightsBeforeSpecificTime(List<FlightInfoDTO> flightInfoDTOList, LocalTime time) {
        return flightInfoDTOList.stream()
                .filter(f -> f.getDeparture() != null)
                .filter(f -> f.getDeparture().toLocalTime().isBefore(time))
                .toList();
    }

    // Opgave 4
    public static List<FlightDTO> totalFlightForAirline(List<FlightDTO> flightList) {
        return flightList.stream()
                .filter(flight -> flight.getAirline() != null
                        && flight.getAirline().getName() != null
                        && flight.getAirline().getName().equalsIgnoreCase("Lufthansa"))
                .collect(Collectors.toList());
    }

    // Opgave 5
    // Add a new feature (make a list of all flights sorted by arrival time)
    public static List<FlightInfoDTO> listOfFlightsSortedByArrival(List<FlightInfoDTO> flightInfoDTOList) {
        return flightInfoDTOList.stream()
                .sorted(Comparator.comparing(FlightInfoDTO::getArrival))
                .collect(Collectors.toList());
    }

    // Opgave 6 (AI Løst)
    // Add a new feature (calculate the total flight time for each airline)
    public static Map<String, Long> totalFlightTimePerAirline(List<FlightInfoDTO> flightInfoDTOList) {
        return flightInfoDTOList.stream()
                .filter(f -> f.getAirline() != null && f.getDuration() != null)
                .collect(Collectors.groupingBy(
                        FlightInfoDTO::getAirline,
                        Collectors.summingLong(f -> f.getDuration().toMinutes())
                ));
    }

    // Opgave 7
    // Add a new feature (make a list of all flights sorted by duration)
    public static List<FlightInfoDTO> listOfAllFlightsSortedByDuration(List<FlightInfoDTO> flightInfoDTOList){
        return flightInfoDTOList.stream()
                .sorted(Comparator.comparing(FlightInfoDTO::getDuration))
                .collect(Collectors.toList());
    }
}
