package com.github.zoltanmeze.aoc.day17;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.AllArgsConstructor;
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
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Scanner;

@Slf4j
public class Day17 implements Runnable {

    public static void main(String[] args) {
        new Day17().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        int[][] input = parseInput();

        Coordinate start = Coordinate.of(0, 0);
        Coordinate end = Coordinate.of(input[0].length - 1, input.length - 1);

        return calculateLeastHeatLoss(start, end, input, 0, 3);
    }

    private Object partTwo() {
        int[][] input = parseInput();

        Coordinate start = Coordinate.of(0, 0);
        Coordinate end = Coordinate.of(input[0].length - 1, input.length - 1);

        return calculateLeastHeatLoss(start, end, input, 4, 10);
    }

    private int calculateLeastHeatLoss(Coordinate start, Coordinate end, int[][] map, int minSameDirection, int maxSameDirection) {
        PriorityQueue<State> queue = new PriorityQueue<>();
        queue.add(State.of(start, Direction.RIGHT, 0));
        queue.add(State.of(start, Direction.DOWN, 0));

        Integer[][][] heatLosses = new Integer[map.length][map[0].length][4];

        State state;
        while (!queue.isEmpty() && !(state = queue.poll()).coordinate.equals(end)) {
            Coordinate coordinate = state.coordinate;
            Direction direction = state.direction;

            Integer heatLoss = heatLosses[coordinate.y][coordinate.x][direction.ordinal()];
            if (heatLoss != null && state.heatLoss > heatLoss) {
                continue;
            }
            heatLoss = state.heatLoss;
            for (int i = 1; i <= maxSameDirection; i++) {
                coordinate = coordinate.move(direction);
                if (!coordinate.inBounds(start, end)) {
                    break;
                }
                heatLoss += map[coordinate.y][coordinate.x];
                if (i < minSameDirection) {
                    continue;
                }
                for (Direction turnDirection : direction.turn()) {
                    Integer previousHeatLoss = heatLosses[coordinate.y][coordinate.x][turnDirection.ordinal()];
                    if (previousHeatLoss == null || heatLoss < previousHeatLoss) {
                        queue.offer(State.of(coordinate, turnDirection, heatLoss));
                        heatLosses[coordinate.y][coordinate.x][turnDirection.ordinal()] = heatLoss;
                    }
                }
            }
        }
        return Arrays.stream(heatLosses[end.y][end.x])
            .filter(Objects::nonNull)
            .reduce(Math::min)
            .orElseThrow();
    }

    @Data(staticConstructor = "of")
    private static class State implements Comparable<State> {
        private final Coordinate coordinate;
        private final Direction direction;
        private final int heatLoss;

        @Override
        public int compareTo(State o) {
            return heatLoss - o.heatLoss;
        }
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    private static class Coordinate {
        private final int x;
        private final int y;

        public Coordinate add(Coordinate other) {
            return Coordinate.of(x + other.x, y + other.y);
        }

        public Coordinate move(Direction direction) {
            return add(direction.next);
        }

        public boolean inBounds(Coordinate start, Coordinate end) {
            return x >= start.x && y >= start.y && x <= end.x && y <= end.y;
        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum Direction {

        UP(Coordinate.of(0, -1)),
        LEFT(Coordinate.of(-1, 0)),
        DOWN(Coordinate.of(0, 1)),
        RIGHT(Coordinate.of(1, 0));

        private final Coordinate next;

        public Direction[] turn() {
            return new Direction[] {
                Direction.values()[(ordinal() + 1) % Direction.values().length],
                Direction.values()[(ordinal() + 3) % Direction.values().length]
            };
        }
    }

    @SneakyThrows
    private int[][] parseInput() {
        File file = ResourceUtils.getResourceFile("day17.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            List<int[]> map = new ArrayList<>();
            while (scanner.hasNextLine()) {
                map.add(scanner.nextLine()
                    .codePoints()
                    .map(x -> Integer.parseInt(String.valueOf((char) x)))
                    .toArray());
            }
            return map.toArray(new int[0][]);
        }
    }
}
