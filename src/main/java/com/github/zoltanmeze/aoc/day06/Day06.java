package com.github.zoltanmeze.aoc.day06;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Slf4j
public class Day06 implements Runnable {

    public static void main(String[] args) {
        new Day06().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        Race[] input = parseInput();

        int results = 1;
        for (Race race : input) {
            int minimumTime = 0;
            do {
                minimumTime = (int) Math.ceil((race.distance + 1d) / (race.time - minimumTime));
            } while (((race.time - minimumTime) * minimumTime) <= race.distance);

            results *= race.time + 1 - 2 * minimumTime;
        }
        return results;
    }

    public Object partTwo() {
        Race[] input = parseInput();

        long time = 0;
        long distance = 0;

        for (Race value : input) {
            time = append(time, value.time);
            distance = append(distance, value.distance);
        }

        int minimumTime = 0;
        do {
            minimumTime = (int) Math.ceil((distance + 1d) / (time - minimumTime));
        } while (((time - minimumTime) * minimumTime) <= distance);

        return time + 1 - 2L * minimumTime;
    }

    private long append(long target, int number) {
        if (number == 0) {
            return target;
        }
        return append(target, number / 10) * 10 + number % 10;
    }

    @Data(staticConstructor = "of")
    private static class Race {
        private final int time;
        private final int distance;
    }

    @SneakyThrows
    private Race[] parseInput() {
        File file = ResourceUtils.getResourceFile("day06.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            Pattern pattern = Pattern.compile("\\d+");
            if (!scanner.hasNextLine()) {
                throw new RuntimeException();
            }
            int[] times = pattern.matcher(scanner.nextLine()).results()
                .map(MatchResult::group)
                .mapToInt(Integer::parseInt)
                .toArray();

            if (!scanner.hasNextLine()) {
                throw new RuntimeException();
            }
            int[] distances = pattern.matcher(scanner.nextLine()).results()
                .map(MatchResult::group)
                .mapToInt(Integer::parseInt)
                .toArray();

            Race[] races = new Race[times.length];
            for (int i = 0; i < times.length; i++) {
                races[i] = Race.of(times[i], distances[i]);
            }
            return races;
        }
    }
}
