package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import java.util.*;
import java.util.stream.Collectors;

public class GoalFind {

    public Set<List<BEvent>> goalFindSuite(Set<List<BEvent>> testSuite) {
        Set<List<BEvent>> newTestSuite = new HashSet<>();

        for (var test : testSuite) {

            newTestSuite.add(goalInList(test));
        }
        return newTestSuite;
    }

    public Set<List<BEvent>> goalFindSuiteAfter(Set<List<BEvent>> testSuite) {
        Set<List<BEvent>> newTestSuite = new HashSet<>();

        for (var test : testSuite) {

            newTestSuite.add(goalInListAfter(test));
        }
        return newTestSuite;
    }

    public Set<List<BEvent>> goalFindSuiteKuhn(Set<List<BEvent>> testSuite) {
        Set<List<BEvent>> newTestSuite = new HashSet<>();

        for (var test : testSuite) {

            newTestSuite.add(goalInListKuhn(test));
        }
        return newTestSuite;
    }

    public List<BEvent> goalInList(List<BEvent> test){

        List<BEvent> valueList = new ArrayList<>();

        Map<String, Long> eventMap = test.stream().collect(Collectors.groupingBy(e -> e.name, Collectors.counting()));
        eventMap.entrySet().removeIf(entry -> entry.toString().startsWith("Context"));

        Collection<Long> evalues = eventMap.values();
        Long[] value = evalues.toArray(new Long[0]);

        Set<String> kkk = eventMap.keySet();
        String[] key = kkk.toArray(new String[0]);

        for (BEvent item : test)
            if (!item.name.startsWith("Context"))
                valueList.add(item);

        for (int i = 1; i <= 1; i++) {
            for (int j = 1; j <= 1; j++) {
                for (int x = 0; x < key.length && value[x] >= i; x++) {
                    for (int y = x; y < key.length && value[y] >= j; y++) {
                        String goal = "Goal:" + i + "x" + key[x] + "->" + j + "x" + key[y];
                        valueList.add(new BEvent(goal));
                    }
                }
            }
        }
        return valueList;
    }

    public List<BEvent> goalInListAfter(List<BEvent> test){

        List<BEvent> valueList = new ArrayList<>();

        Map<String, Long> eventMap = test.stream().collect(Collectors.groupingBy(e -> e.name, Collectors.counting()));
        eventMap.entrySet().removeIf(entry -> entry.toString().startsWith("Context"));

        Collection<Long> evalues = eventMap.values();
        Long[] value = evalues.toArray(new Long[0]);

        Set<String> keys = eventMap.keySet();
        String[] key = keys.toArray(new String[0]);

        for (BEvent item : test)
            if (!item.name.startsWith("Context"))
                valueList.add(item);

        for (int x = 0; x < key.length-1; x++) {
                String goal = "Goal:" + key[x] + "->" + key[x+1];
                valueList.add(new BEvent(goal));
        }
        return valueList;
    }

    public List<BEvent> goalInListKuhn(List<BEvent> test){

        List<BEvent> valueList = new ArrayList<>();

        Map<String, Long> eventMap = test.stream().collect(Collectors.groupingBy(e -> e.name, Collectors.counting()));
        eventMap.entrySet().removeIf(entry -> entry.toString().startsWith("Context"));

        Collection<Long> evalues = eventMap.values();
        Long[] value = evalues.toArray(new Long[0]);

        Set<String> kkk = eventMap.keySet();
        String[] key = kkk.toArray(new String[0]);

        for (BEvent item : test)
            if (!item.name.startsWith("Context"))
                valueList.add(item);

        for (int x = 0; x < key.length ; x++) {
            for (int y = x; y < key.length ; y++) {
                String goal = "Goal:" + key[x] + "->" + key[y];
                valueList.add(new BEvent(goal));
            }
        }
        return valueList;
    }


}
