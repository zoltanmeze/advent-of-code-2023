package com.github.zoltanmeze.aoc.day14;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class Day14 implements Runnable {

    public static void main(String[] args) {
        new Day14().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        char[][] dish = parseInput();

        tiltNorth(dish);

        return count(dish); // Possible to count in tilt
    }

    public Object partTwo() {
        char[][] dish = parseInput();

        int maxCycles = 1_000_000_000;

        Map<String, Integer> cycles = new HashMap<>();

        for (int i = 0; i < maxCycles; i++) {
            Integer cycle = cycles.putIfAbsent(toString(dish), i);
            if (cycle != null && i + (i - cycle) < maxCycles) {
                int distance = i - cycle;
                int next = i + (distance * ((maxCycles - i) / distance));
                log.info("Cycle loop detected from {} to {} with distance {}, skipping to {}", i, cycle, distance, next);
                i = next - 1;
                continue;
            }
            for (int x = 0; x < 4; x++) {
                tiltNorth(dish); // Not going to implement for each direction, easier to just rotate the dish
                rotate(dish);
            }
        }
        return count(dish);
    }

    private long count(char[][] dish) {
        long sum = 0L;
        for (int i = 0; i < dish.length; i++) {
            for (int j = 0; j < dish[i].length; j++) {
                if (dish[i][j] == 'O') {
                    sum += dish.length - i;
                }
            }
        }
        return sum;
    }

    private void tiltNorth(char[][] dish) {
        for (int i = 0; i < dish[0].length; i++) {
            int last = 0;
            for (int j = 0; j < dish.length; j++) {
                if (dish[j][i] == '#' || (last == j && dish[j][i] == 'O')) {
                    last = j + 1;
                    continue;
                } else if (dish[j][i] != 'O') {
                    continue;
                }
                dish[last][i] = dish[j][i];
                dish[j][i] = '.';
                last++;
            }
        }
    }

    private void rotate(char[][] dish) {
        for (int j = 0; j < dish.length / 2; j++) {
            for (int i = j; i < dish.length - j - 1; i++) {
                char temp = dish[j][i];
                dish[j][i] = dish[dish.length - 1 - i][j];
                dish[dish.length - 1 - i][j] = dish[dish.length - 1 - j][dish.length - 1 - i];
                dish[dish.length - 1 - j][dish.length - 1 - i] = dish[i][dish.length - 1 - j];
                dish[i][dish.length - 1 - j] = temp;
            }
        }
    }

    private String toString(char[][] dish) {
        return Arrays.stream(dish)
            .parallel()
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
    }

    @SneakyThrows
    private char[][] parseInput() {
        File file = ResourceUtils.getResourceFile("day14.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            List<char[]> dish = new ArrayList<>();
            while (scanner.hasNextLine()) {
                dish.add(scanner.nextLine().toCharArray());
            }
            return dish.toArray(new char[0][]);
        }
    }
}
