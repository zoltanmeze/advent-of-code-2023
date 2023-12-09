package com.github.zoltanmeze.aoc.day09;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

@Slf4j
public class Day09 implements Runnable {

    public static void main(String[] args) {
        new Day09().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        List<int[]> input = parseInput();

        return input.stream()
            .mapToInt(history -> extrapolateNext(history, false))
            .reduce(0, Math::addExact);
    }

    public Object partTwo() {
        List<int[]> input = parseInput();

        return input.stream()
            .mapToInt(history -> extrapolateNext(history, true))
            .reduce(0, Math::addExact);
    }

    private int extrapolateNext(int[] history, boolean backwards) {
        Stack<Integer> stack = new Stack<>();
        int[] current = history;
        int sum = 1;
        while (sum != 0) {
            sum = 0;
            int[] next = new int[current.length - 1];
            for (int i = 0; i < current.length - 1; i++) {
                next[i] = current[i + 1] - current[i];
                sum |= next[i];
            }
            stack.add(current[backwards ? 0 : current.length - 1]);
            current = next;
        }
        var next = 0;
        var signum = backwards ? -1 : 1;
        while (!stack.isEmpty()) {
            next = stack.pop() + signum * next;
        }
        return next;
    }

    @SneakyThrows
    private List<int[]> parseInput() {
        File file = ResourceUtils.getResourceFile("day09.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            List<int[]> histories = new ArrayList<>();
            while (scanner.hasNextLine()) {
                int[] history = Arrays.stream(scanner.nextLine().split("\\s+"))
                    .mapToInt(Integer::parseInt)
                    .toArray();

                histories.add(history);
            }
            return histories;
        }
    }
}
