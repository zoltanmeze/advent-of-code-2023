package com.github.zoltanmeze.aoc.day04;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Day04 implements Runnable {

    public static void main(String[] args) {
        new Day04().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        ScratchCard[] input = parseInput();
        long result = 0;
        for (var game : input) {
            int matches = game.numberOfMatches();
            if (matches == 0) {
                continue;
            }
            result += 1L << (matches - 1);
        }
        return result;
    }

    public Object partTwo() {
        ScratchCard[] input = parseInput();
        int[] cards = new int[input.length];
        long result = 0;
        for (int i = 0; i < input.length; i++) {
            int multiplier = ++cards[i];
            int matches = input[i].numberOfMatches();
            for (int j = i + 1; j <= i + matches && j < input.length; j++) {
                cards[j] += multiplier;
            }
            result += multiplier;
        }
        return result;
    }

    @Data(staticConstructor = "of")
    private static class ScratchCard {
        private final int id;
        private final BitSet winning;
        private final BitSet played;

        public BitSet matches() {
            BitSet copy = (BitSet) winning.clone();
            copy.and(played);
            return copy;
        }

        public int numberOfMatches() {
            return matches().cardinality();
        }
    }

    @SneakyThrows
    private ScratchCard[] parseInput() {
        File file = ResourceUtils.getResourceFile("day04.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            Pattern gamePattern = Pattern.compile("^Card\\s+(?<ID>\\d+): ");
            Pattern numberPattern = Pattern.compile("\\d+");
            List<ScratchCard> games = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Matcher matcher = gamePattern.matcher(line);
                if (!matcher.find()) {
                    throw new RuntimeException();
                }
                int id = Integer.parseInt(matcher.group("ID"));
                List<BitSet> numberSets = new ArrayList<>(2);
                try (Scanner scanner1 = new Scanner(line.substring(matcher.end()))) {
                    scanner1.useDelimiter("\\|");
                    while (scanner1.hasNext()) {
                        try (Scanner scanner2 = new Scanner(scanner1.next())) {
                            BitSet numbers = scanner2.findAll(numberPattern)
                                .map(MatchResult::group)
                                .map(Integer::parseInt)
                                .collect(BitSet::new, BitSet::set, BitSet::or);
                            numberSets.add(numbers);
                        }
                    }
                }
                if (numberSets.size() != 2) {
                    throw new RuntimeException();
                }
                games.add(ScratchCard.of(id, numberSets.get(0), numberSets.get(1)));
            }
            return games.toArray(new ScratchCard[0]);
        }
    }
}
