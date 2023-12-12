package com.github.zoltanmeze.aoc.day12;

import com.github.zoltanmeze.aoc.utilities.EnumUtils;
import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class Day12 implements Runnable {

    public static void main(String[] args) {
        new Day12().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        List<Record> input = parseInput();

        return input.stream()
            .mapToLong(this::findArrangements)
            .reduce(0L, Long::sum);
    }

    public Object partTwo() {
        List<Record> input = parseInput();

        return input.stream()
            .map(record -> record.unfold(5))
            .mapToLong(this::findArrangements)
            .reduce(0L, Long::sum);
    }

    private long findArrangements(Record record) {
        Long[][] dp = new Long[record.springs.length][record.groups.length];
        return findArrangements(record.springs, 0, record.groups, 0, dp);
    }

    private long findArrangements(Spring[] springs, int position, int[] groups, int index, Long[][] dp) {
        if (index == groups.length) {
            return containsDamagedSprings(springs, position) ? 0 : 1;
        }
        int group = groups[index];
        if (position > springs.length - 1) {
            return 0L; // Ran out of positions, but some groups weren't visited
        } else if (dp[position][index] != null) {
            return dp[position][index];
        }
        long sum = 0L;
        boolean mustFit = false;
        for (int i = position; i <= springs.length - group && !mustFit; i++) {
            if (i > 0 && springs[i - 1] == Spring.DAMAGED) {
                break;
            } else if (springs[i] == Spring.OPERATIONAL) {
                continue;
            }
            boolean canFit = true;
            for (int s = i; canFit && s < i + group; s++) {
                if (!(canFit = springs[s] != Spring.OPERATIONAL)) {
                    i = s; // Skip over this range, saves few iterations
                } else {
                    mustFit |= springs[s] == Spring.DAMAGED;
                }
            }
            if (canFit && (i + group >= springs.length || springs[i + group] != Spring.DAMAGED)) {
                sum += findArrangements(springs, i + group + 1, groups, index + 1, dp);
            }
            mustFit &= !canFit;
        }
        dp[position][index] = sum;
        return sum;
    }

    private boolean containsDamagedSprings(Spring[] springs, int fromPosition) {
        boolean contains = false;
        for (int i = fromPosition; !contains && i < springs.length; i++) {
            contains = springs[i] == Spring.DAMAGED;
        }
        return contains;
    }

    @Data(staticConstructor = "of")
    private static class Record {

        private final Spring[] springs;
        private final int[] groups;

        public Record unfold(int times) {
            Spring[] unfoldedSprings = new Spring[springs.length * times + times - 1];
            int[] unfoldedGroups = new int[groups.length * times];
            for (int i = 0; i < times; i++) {
                if (i > 0) {
                    unfoldedSprings[i * springs.length + i - 1] = Spring.UNKNOWN;
                }
                System.arraycopy(groups, 0, unfoldedGroups, i * groups.length, groups.length);
                System.arraycopy(springs, 0, unfoldedSprings, i * springs.length + i, springs.length);
            }
            return Record.of(unfoldedSprings, unfoldedGroups);
        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum Spring {

        OPERATIONAL('.'),
        DAMAGED('#'),
        UNKNOWN('?');

        private final char label;

        private static final Map<Character, Spring> REVERSE_LOOKUP_MAP = EnumUtils.toReverseLookupMap(Spring.class, Spring::getLabel);

        public static Spring fromLabel(char label) {
            return REVERSE_LOOKUP_MAP.get(label);
        }

        @Override
        public String toString() {
            return String.valueOf(label);
        }
    }

    @SneakyThrows
    private List<Record> parseInput() {
        File file = ResourceUtils.getResourceFile("day12.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            List<Record> records = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String[] record = scanner.nextLine().split("\\s");

                Spring[] springs = record[0].codePoints()
                    .mapToObj(x -> Spring.fromLabel(Character.toChars(x)[0]))
                    .toArray(Spring[]::new);

                int[] groups = Arrays.stream(record[1].split(","))
                    .mapToInt(Integer::parseInt)
                    .toArray();

                records.add(Record.of(springs, groups));
            }
            return records;
        }
    }
}
