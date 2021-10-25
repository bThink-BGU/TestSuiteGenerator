package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class GoalFind {

    public Set<List<String>> goalFindSuite(Set<List<String>> testSuite) {
        Set<List<String>> newTestSuite = new HashSet<>();

        for (var test : testSuite) {

            newTestSuite.add(goalInList(test));
        }
        return newTestSuite;
    }

    public Set<List<String>> goalFindSuiteAfter(Set<List<String>> testSuite) {
        Set<List<String>> newTestSuite = new HashSet<>();

        for (var test : testSuite) {

            newTestSuite.add(goalInListAfter(test));
        }
        return newTestSuite;
    }

    public Set<List<String>> goalFindSuiteKuhn(@NotNull Set<List<String>> testSuite) {
        Set<List<String>> newTestSuite = new HashSet<>();

        for (var test : testSuite) {

            newTestSuite.add(goalInListKuhn(test));
        }
        return newTestSuite;
    }

    public List<String> goalInList(List<String> test){

        List<String> valueList = new ArrayList<>();

        Map<String, Long> eventMap = test.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        eventMap.entrySet().removeIf(entry -> entry.toString().startsWith("Context"));

        Collection<Long> evalues = eventMap.values();
        Long[] value = evalues.toArray(new Long[0]);

        Set<String> kkk = eventMap.keySet();
        String[] key = kkk.toArray(new String[0]);

        for (String item : test)
            if (!item.startsWith("Context"))
                valueList.add(item);

        for (int i = 1; i <= 1; i++) {
            for (int j = 1; j <= 1; j++) {
                for (int x = 0; x < key.length && value[x] >= i; x++) {
                    for (int y = x; y < key.length && value[y] >= j; y++) {
                        String goal = "Goal:" + i + "x" + key[x] + "->" + j + "x" + key[y];
                        valueList.add(goal);
                    }
                }
            }
        }
        return valueList;
    }

    public List<String> goalInListAfter(List<String> test){

        List<String> valueList = new ArrayList<>();

        Map<String, Long> eventMap = test.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        eventMap.entrySet().removeIf(entry -> entry.toString().startsWith("Context"));
        System.out.println("Map-"+eventMap.toString());


        Collection<Long> evalues = eventMap.values();
        Long[] value = evalues.toArray(new Long[0]);

        Set<String> keys = eventMap.keySet();
        String[] key = keys.toArray(new String[0]);

//        for (String item : test)
//            if (!item.name.startsWith("Context"))
//                valueList.add(item);

        for (int x = 0; x < key.length-1; x++) {
                String goal = "Goal:" + key[x] + "->" + key[x+1];
                valueList.add(goal);
        }
        return valueList;
    }

    public List<String> goalInListKuhn(List<String> test){

        List<String> valueList = new ArrayList<>();

        Map<String, Long> eventMap = test.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        eventMap.entrySet().removeIf(entry -> entry.toString().startsWith("Context"));

        Collection<Long> evalues = eventMap.values();
        Long[] value = evalues.toArray(new Long[0]);

        Set<String> kkk = eventMap.keySet();
        String[] key = kkk.toArray(new String[0]);

        for (String item : test)
            if (!item.startsWith("Context"))
                valueList.add(item);

        for (int x = 0; x < key.length ; x++) {
            for (int y = x; y < key.length ; y++) {
                String goal = "Goal:" + key[x] + "->" + key[y];
                valueList.add(goal);
            }
        }
        return valueList;
    }


}
