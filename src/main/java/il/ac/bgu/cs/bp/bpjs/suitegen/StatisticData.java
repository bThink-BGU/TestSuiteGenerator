package il.ac.bgu.cs.bp.bpjs.suitegen;

import java.lang.reflect.Array;
import java.util.Arrays;

public class StatisticData {

    int[] towWayKuhn = new int[1000];
    int[] towWayOur = new int[1000];
    int[] towWayRandom = new int[1000];
    int[] towWayOptimal = new int[1000];
    int[] threeWayKuhn = new int[1000];
    int[] threeWayOur = new int[1000];
    int[] threeWayRandom = new int[1000];
    int[] threeWayOptimal = new int[1000];


    @Override
    public String toString() {
        return "StatisticData{" +
                "towWayKuhn=" + Arrays.toString(towWayKuhn) +
                ", towWayOur=" + Arrays.toString(towWayOur) +
                ", towWayRandom=" + Arrays.toString(towWayRandom) +
                ", towWayOptimal=" + Arrays.toString(towWayOptimal) +
                ", threeWayKuhn=" + Arrays.toString(threeWayKuhn) +
                ", threeWayOur=" + Arrays.toString(threeWayOur) +
                ", threeWayRandom=" + Arrays.toString(threeWayRandom) +
                ", threeWayOptimal=" + Arrays.toString(threeWayOptimal) +
                '}';
    }

}
