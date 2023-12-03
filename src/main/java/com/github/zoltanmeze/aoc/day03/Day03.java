package com.github.zoltanmeze.aoc.day03;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Slf4j
public class Day03 implements Runnable {

    public static void main(String[] args) {
        new Day03().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        char[][] input = parseInput();
        long sum = 0L;
        for (int y = 0; y < input.length; y++) {
            for (int x = 0; x < input[y].length; ) {
                char ch = input[y][x];
                if (!isNumber(ch)) {
                    x++;
                    continue;
                }
                long number = 0L;
                int x1 = x;
                do {
                    number *= 10;
                    number += ch - '0';
                } while (++x < input[y].length && (isNumber(ch = input[y][x])));
                if (checkAdjacent(x1, y, x - 1, y, input, this::isSymbol)) {
                    sum += number;
                }
            }
        }
        return sum;
    }

    public Object partTwo() {
        char[][] input = parseInput();
        Map<Integer, Map<Integer, List<Long>>> results = new HashMap<>();
        for (int y = 0; y < input.length; y++) {
            for (int x = 0; x < input[y].length; ) {
                char ch = input[y][x];
                if (!isNumber(ch)) {
                    x++;
                    continue;
                }
                long number = 0L;
                int x1 = x;
                do {
                    number *= 10;
                    number += ch - '0';
                } while (++x < input[y].length && (isNumber(ch = input[y][x])));
                long finalNumber = number;
                checkAdjacent(x1, y, x - 1, y, input, c -> c == '*', (targetX, targetY) ->
                    results.computeIfAbsent(targetY, HashMap::new)
                        .computeIfAbsent(targetX, ArrayList::new)
                        .add(finalNumber));
            }
        }
        return results.values()
            .stream()
            .flatMap(x -> x.values()
                .stream()
                .filter(y -> y.size() == 2)
                .map(y -> y.stream().reduce(1L, Math::multiplyExact)))
            .reduce(0L, Math::addExact);
    }

    private boolean checkAdjacent(int x1, int y1, int x2, int y2, char[][] arr, Predicate<Character> predicate) {
        return checkAdjacent(x1, y1, x2, y2, arr, ((arr1, x, y) -> predicate.test(arr1[y][x])), true);
    }

    private void checkAdjacent(int x1, int y1, int x2, int y2, char[][] arr, Predicate<Character> predicate, BiConsumer<Integer, Integer> consumer) {
        checkAdjacent(x1, y1, x2, y2, arr, ((arr1, x, y) -> {
            if (predicate.test(arr[y][x])) {
                consumer.accept(x, y);
                return true;
            }
            return false;
        }), false);
    }

    private boolean checkAdjacent(int x1, int y1, int x2, int y2, char[][] arr, AdjacentPredicate predicate, boolean lazy) {
        if (x1 > x2 || y1 > y2) {
            throw new RuntimeException("Invalid arguments");
        } else if (y1 < 0 || y2 >= arr.length || x1 < 0 || x2 >= arr[y1].length) {
            throw new RuntimeException("Out of range");
        }
        boolean valid = false;
        boolean[] checkY = {y1 - 1 >= 0, y2 + 1 < arr.length};
        for (int x = Math.max(x1 - 1, 0), maxX = Math.min(x2 + 1, arr[y1].length - 1); x <= maxX && (!lazy || !valid); x++) {
            valid = (checkY[0] && predicate.test(arr, x, y1 - 1)) || (checkY[1] && predicate.test(arr, x, y2 + 1));
        }
        boolean[] checkX = {x1 - 1 >= 0, x2 + 1 < arr[y1].length};
        for (int y = y1; y <= y2 && (!lazy || !valid); y++) {
            valid = (checkX[0] && predicate.test(arr, x1 - 1, y)) || (checkX[1] && predicate.test(arr, x2 + 1, y));
        }
        return valid;
    }

    private boolean isNumber(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isSymbol(char ch) {
        return ch != '.' && !isNumber(ch);
    }

    @FunctionalInterface
    private interface AdjacentPredicate {
        boolean test(char[][] arr, int x, int y);
    }

    @SneakyThrows
    private char[][] parseInput() {
        File file = ResourceUtils.getResourceFile("day03.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            List<char[]> lines = new ArrayList<>();
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine().toCharArray());
            }
            return lines.toArray(new char[0][]);
        }
    }
}
