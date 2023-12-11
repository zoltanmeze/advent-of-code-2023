package com.github.zoltanmeze.aoc.day11;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.function.BiFunction;

@Slf4j
public class Day11 implements Runnable {

    public static void main(String[] args) {
        new Day11().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    private Object partOne() {
        Universe input = parseInput();

        var galaxies = expandSpaceBetweenGalaxies(input, 1);

        long sum = 0;
        for (int i = 0; i < galaxies.length - 1; i++) {
            for (int j = i + 1; j < galaxies.length; j++) {
                sum += galaxies[i].manhattanDistance(galaxies[j]);
            }
        }
        return sum;
    }

    private Object partTwo() {
        Universe input = parseInput();

        var galaxies = expandSpaceBetweenGalaxies(input, 1000000 - 1);

        long sum = 0;
        for (int i = 0; i < galaxies.length - 1; i++) {
            for (int j = i + 1; j < galaxies.length; j++) {
                sum += galaxies[i].manhattanDistance(galaxies[j]);
            }
        }
        return sum;
    }

    private Galaxy[] expandSpaceBetweenGalaxies(Universe universe, int expansion) {
        TreeMap<Integer, List<Integer>> galaxiesByY = new TreeMap<>();
        TreeMap<Integer, List<Integer>> galaxiesByX = new TreeMap<>();

        Galaxy[] galaxies = Arrays.copyOf(universe.galaxies, universe.galaxies.length);

        for (int i = 0; i < galaxies.length; i++) {
            galaxiesByY.computeIfAbsent(galaxies[i].y, ArrayList::new).add(i);
            galaxiesByX.computeIfAbsent(galaxies[i].x, ArrayList::new).add(i);
        }
        moveGalaxies(universe.width, galaxies, galaxiesByX, (galaxy, empySpaceSize) -> galaxy.moveByX(expansion * empySpaceSize));
        moveGalaxies(universe.height, galaxies, galaxiesByY, (galaxy, empySpaceSize) -> galaxy.moveByY(expansion * empySpaceSize));
        return galaxies;
    }

    private void moveGalaxies(int size, Galaxy[] galaxies,
                              TreeMap<Integer, List<Integer>> galaxiesBySpace,
                              BiFunction<Galaxy, Integer, Galaxy> mapping) {
        for (int emptySpace = 0, emptySpaceSize = 0; emptySpace < size; emptySpaceSize = 0) {
            while (emptySpace++ < size && !galaxiesBySpace.containsKey(emptySpace)) {
                emptySpaceSize++;
            }
            if (emptySpaceSize == 0) {
                continue;
            }
            for (var galaxyIndexes : galaxiesBySpace.tailMap(emptySpace).values()) {
                for (var galaxyIndex : galaxyIndexes) {
                    galaxies[galaxyIndex] = mapping.apply(galaxies[galaxyIndex], emptySpaceSize);
                }
            }
        }
    }

    @Data(staticConstructor = "of")
    private static class Galaxy {
        private final int x;
        private final int y;

        public int manhattanDistance(Galaxy other) {
            return Math.abs(x - other.x) + Math.abs(y - other.y);
        }

        public Galaxy moveByX(int expansion) {
            return Galaxy.of(x + expansion, y);
        }

        public Galaxy moveByY(int expansion) {
            return Galaxy.of(x, y + expansion);
        }
    }

    @Data(staticConstructor = "of")
    private static class Universe {
        private final int width;
        private final int height;
        private final Galaxy[] galaxies;
    }

    @SneakyThrows
    private Universe parseInput() {
        File file = ResourceUtils.getResourceFile("day11.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            List<Galaxy> galaxies = new ArrayList<>();
            int x = 0;
            int y;
            for (y = 0; scanner.hasNextLine(); y++) {
                char[] row = scanner.nextLine().toCharArray();
                for (x = 0; x < row.length; x++) {
                    char ch = row[x];
                    if (ch == '#') {
                        galaxies.add(Galaxy.of(x, y));
                    }
                }
            }
            return Universe.of(x, y, galaxies.toArray(new Galaxy[0]));
        }
    }
}
