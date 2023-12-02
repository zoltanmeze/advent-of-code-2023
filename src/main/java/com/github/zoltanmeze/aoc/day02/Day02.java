package com.github.zoltanmeze.aoc.day02;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Day02 implements Runnable {

    public static void main(String[] args) {
        new Day02().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        List<Game> input = parseInput();

        int[] limits = new int[Color.values().length];
        limits[Color.RED.ordinal()] = 12;
        limits[Color.GREEN.ordinal()] = 13;
        limits[Color.BLUE.ordinal()] = 14;

        int sum = 0;
        for (Game game : input) {
            boolean valid = true;
            for (int i = 0; i < game.picks.length && valid; i++) {
                int[] current = {0, 0, 0};
                Pick[] picks = game.picks[i];
                for (Pick pick : picks) {
                    if ((current[pick.color.ordinal()] += pick.number) > limits[pick.color.ordinal()]) {
                        valid = false;
                        break;
                    }
                }
            }
            if (valid) {
                sum += game.id;
            }
        }
        return sum;
    }

    public Object partTwo() {
        List<Game> input = parseInput();
        long sum = 0;
        for (Game game : input) {
            int[] required = {0, 0, 0};
            for (int i = 0; i < game.picks.length; i++) {
                int[] curr = {0, 0, 0};
                Pick[] picks = game.picks[i];
                for (Pick pick : picks) {
                    if ((curr[pick.color.ordinal()] += pick.number) > required[pick.color.ordinal()]) {
                        required[pick.color.ordinal()] = curr[pick.color.ordinal()];
                    }
                }
            }
            long pow = 1;
            for (int r : required) {
                pow *= r;
            }
            sum += pow;
        }
        return sum;
    }

    private enum Color {
        RED, GREEN, BLUE
    }

    @Data(staticConstructor = "of")
    private static final class Pick {
        private final Color color;
        private final int number;
    }

    @Data(staticConstructor = "of")
    private static final class Game {
        private final int id;
        private final Pick[][] picks;
    }

    @SneakyThrows
    private List<Game> parseInput() {
        File file = ResourceUtils.getResourceFile("day02.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            Pattern gamePattern = Pattern.compile("^Game (?<ID>\\d+): ");
            Pattern pickPattern = Pattern.compile("(?<NUMBER>\\d+) (?<COLOR>(red|green|blue))");

            List<Game> games = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                Matcher matcher = gamePattern.matcher(line);
                if (!matcher.find()) {
                    throw new RuntimeException();
                }
                int id = Integer.parseInt(matcher.group("ID"));

                List<Pick[]> pickSets = new ArrayList<>();
                try (Scanner scanner1 = new Scanner(line.substring(matcher.end()))) {
                    scanner1.useDelimiter(";");
                    while (scanner1.hasNext()) {
                        List<Pick> picks = new ArrayList<>();
                        matcher = pickPattern.matcher(scanner1.next());
                        while (matcher.find()) {
                            int number = Integer.parseInt(matcher.group("NUMBER"));
                            Color color = Color.valueOf(matcher.group("COLOR").toUpperCase());
                            picks.add(Pick.of(color, number));
                        }
                        pickSets.add(picks.toArray(new Pick[0]));
                    }
                }
                games.add(Game.of(id, pickSets.toArray(new Pick[0][])));
            }
            return games;
        }
    }
}
