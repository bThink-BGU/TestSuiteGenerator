package il.ac.bgu.cs.bp.bpjs.myAbp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunTestSuite {

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void main(final String[] args) throws Exception {

        AbpTester abpTester = new AbpTester();

        List<String> list = new ArrayList<>();

        abpTester.senderSimulator.setTO_BE_SENT(List.of("A","B","C","D","E","V"));

        try (BufferedReader br = new BufferedReader(new FileReader("KhunTestSuite.txt"))) {
//        try (BufferedReader br = new BufferedReader(new FileReader("BestTestSuite.txt"))) {
            list = br.lines().collect(Collectors.toList());
            ListIterator<String> iterator = list.listIterator(0);

            // Printing the iterated value
            System.out.println("\nUsing ListIterator"+" from Index 1:\n");
            while (iterator.hasNext()) {
                String events = iterator.next();
                List<String> eventsList = Stream.of(events.split(",", -1))
                                          .collect(Collectors.toList());

                try {
                    ListIterator<String> iterator2 = eventsList.listIterator();
                    while (iterator2.hasNext()) {
                        String eventName = iterator2.next();
                        abpTester.abpSimulator(AbpInfra.externalInput.valueOf(eventName.toUpperCase()));
                    }
                    System.out.println(ANSI_GREEN+"events-"+events.toString()+ANSI_RESET);
                } catch (RuntimeException e) {
                    System.out.println(ANSI_RED+"events-"+events.toString()+ANSI_RESET);

                }
                abpTester.resetInfra();
                abpTester.senderSimulator.setTO_BE_SENT(List.of("A", "B", "C", "D", "E", "V"));

            }
        }
    }
}
