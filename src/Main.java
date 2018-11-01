package app;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {


    public static Random rnd = new Random(7);


    private static long durationListToString = 0L;
    private static long durationStringToList = 0L;

    private static long durationListToByteArray = 0;
    private static long durationByteArrayToList = 0;

    private static long durationListToByteArray2 = 0;
    private static long durationByteArrayToList2 = 0;

    public static void main(String[] args) {

        final int numberOfProfiles = 5_000_000;
        final int sizeOfProfiles = 57;

        generate(numberOfProfiles, sizeOfProfiles)
                .map(l -> listToString(l))
                .map(l -> stringToList(l))
                .count();

        System.out.println("durationListToString = " + durationListToString);
        System.out.println("durationStringToList = " + durationStringToList);


        /*generate(numberOfProfiles, sizeOfProfiles)
                .map(l -> listToByteArray(l))
                .map(l -> byteArrayToList(l))
                .count();

        System.out.println("durationListToByteArray = " + durationListToByteArray);
        System.out.println("durationByteArrayToList = " + durationByteArrayToList);*/


        generate(numberOfProfiles, sizeOfProfiles)
                .map(l -> listToByteArray2(l))
                .map(l -> byteArrayToList2(l))
                .count();

        System.out.println("durationListToByteArray2 = " + durationListToByteArray2);
        System.out.println("durationByteArrayToList2 = " + durationByteArrayToList2);
    }


    static Stream<List<Double>> generate(int streamSize, int listSize) {

        return IntStream.range(0, streamSize)
                .mapToObj(i -> rnd.doubles(listSize)
                                  .boxed()
                                  .collect(Collectors.toList())
                );
    }


    static String listToString(List<Double> values) {

        long start = System.currentTimeMillis();
        String l = String.join(",",
                values.stream().map(String::valueOf).collect(Collectors.toList()));
        long end = System.currentTimeMillis();
        durationListToString += end -start;
        return l;
    }

    static List<Double> stringToList(String values) {

        long start = System.currentTimeMillis();
        List<Double> l = Arrays.stream(values.split(","))
                     .map(Double::valueOf)
                     .collect(Collectors.toList());
        long end = System.currentTimeMillis();
        durationStringToList += end - start;
        return l;
    }

    static byte[] listToByteArray(List<Double> values) {

        long start = System.currentTimeMillis();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.writeObject(values);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] result = bos.toByteArray();
        long end = System.currentTimeMillis();
        durationListToByteArray += end - start;
        return result;
    }

    static List<Double> byteArrayToList(byte[] values)  {

        long start = System.currentTimeMillis();
        ByteArrayInputStream bis = new ByteArrayInputStream(values);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Double> result = null;
        try {
            result = (List<Double>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        durationByteArrayToList += end - start;
        return result;
    }

    static byte[] listToByteArray2(List<Double> values) {

        long start = System.currentTimeMillis();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream oos = new DataOutputStream(bos);
        for (Double d : values) {
            try {
                oos.writeDouble(d);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte[] result = bos.toByteArray();
        long end = System.currentTimeMillis();
        durationListToByteArray2 += end - start;
        return result;
    }

    static List<Double> byteArrayToList2(byte[] values)  {

        long start = System.currentTimeMillis();

        ByteArrayInputStream bis = new ByteArrayInputStream(values);
        DataInputStream ois = new DataInputStream(bis);
        List<Double> result = new ArrayList<>();
        try {
            while (ois.available() > 0) {
                result.add(ois.readDouble());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        durationByteArrayToList2 += end - start;
        return result;
    }




}
