package com.github.zoltanmeze.aoc.day10;

import com.github.zoltanmeze.aoc.utilities.EnumUtils;
import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

@Slf4j
public class Day10 implements Runnable {

    public static void main(String[] args) {
        new Day10().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    private Object partOne() {
        Maze input = parseInput();

        Pipe startingPipe = resolveStartingPipe(input);

        Coordinate current = input.start;
        Direction direction = startingPipe.nextDirections.get(0);
        input.set(current, startingPipe);

        int steps = 0;
        do {
            Direction finalDirection = direction;
            direction = Pipe.fromTile(input.get(current))
                .getNextDirections()
                .stream()
                .filter(nextDirection -> !nextDirection.isOpposite(finalDirection))
                .findFirst()
                .orElseThrow();
            steps++;
            current = current.add(direction.getCoordinate());
        } while (!current.equals(input.start));

        return (steps + 1) / 2;
    }

    private Object partTwo() {
        Maze input = parseInput();

        Pipe startingPipe = resolveStartingPipe(input);

        Maze doubleSizeMaze = input.scaleUp(2);

        Coordinate current = input.start;
        Direction direction = startingPipe.nextDirections.get(1);
        input.set(current, startingPipe);

        do {
            Direction finalDirection = direction;
            Pipe pipe = Pipe.fromTile(input.get(current));
            Coordinate doubleCoordinate = current.scaleUp(2);
            for (var tile : pipe.getTiles()) {
                doubleSizeMaze.set(doubleCoordinate.add(tile), tile.getTile());
            }
            direction = pipe.getNextDirections()
                .stream()
                .filter(direction1 -> !direction1.isOpposite(finalDirection))
                .findFirst()
                .orElseThrow();
            current = current.add(direction.getCoordinate());
        } while (!current.equals(input.start));

        int sum = 0;
        boolean[][] visited = new boolean[doubleSizeMaze.map.length][doubleSizeMaze.map[0].length];
        for (int y = 0; y < input.map.length - 1; y++) {
            for (int x = 0; x < input.map[y].length; x++) {
                if (input.get(x, y) != '.' || visited[y * 2][x * 2]) {
                    continue;
                }
                for (var space : findSpacesEnclosedByLoop(Coordinate.of(x * 2, y * 2), doubleSizeMaze, visited)) {
                    if (space.x % 2 != 0 || space.y % 2 != 0) {
                        continue;
                    }
                    sum++;
                }
            }
        }
        // doubleSizeMaze.print();
        return sum;
    }

    private Set<Coordinate> findSpacesEnclosedByLoop(Coordinate starting, Maze maze, boolean[][] visited) {
        Set<Coordinate> emptySpaces = new HashSet<>();

        Stack<Coordinate> stack = new Stack<>();
        stack.add(starting);

        visited[starting.y][starting.x] = true;

        boolean allInMaze = true;
        while (!stack.isEmpty()) {
            Coordinate current = stack.pop();
            emptySpaces.add(current);
            for (Direction direction : Direction.values()) {
                Coordinate next = current.add(direction.getCoordinate());
                if (maze.notInRange(next)) {
                    allInMaze = false;
                    continue;
                } else if (maze.get(next) != '.' || visited[next.y][next.x]) {
                    continue;
                }
                visited[next.y][next.x] = true;
                stack.add(next);
            }
        }
        if (!allInMaze) {
            return Collections.emptySet();
        }
        return emptySpaces;
    }

    private Pipe resolveStartingPipe(Maze maze) {
        Coordinate start = maze.getStart();
        Set<Direction> directions = new HashSet<>(2);
        for (Direction direction : Direction.values()) {
            Coordinate coordinate = start.add(direction.getCoordinate());
            if (maze.notInRange(coordinate)) {
                continue;
            }
            Pipe pipe = Pipe.fromTile(maze.get(coordinate));
            if (pipe == null) {
                continue;
            }
            for (Direction pipeDirection : pipe.getNextDirections()) {
                if (!pipeDirection.isOpposite(direction)) { // Next pipe can be connected to start
                    continue;
                }
                directions.add(direction);
            }
        }
        return Arrays.stream(Pipe.values())
            .filter(pipe -> directions.containsAll(pipe.getNextDirections()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Unable to resolve start position's pipe type from directions " + directions));
    }

    @Data(staticConstructor = "of")
    private static class Coordinate {

        private final int x;
        private final int y;

        public Coordinate add(Coordinate other) {
            return Coordinate.of(x + other.x, y + other.y);
        }

        public Coordinate scaleUp(int factor) {
            return Coordinate.of(x * factor, y * factor);
        }
    }

    @Data(staticConstructor = "of")
    private static class Maze {
        private final Coordinate start;
        private final char[][] map;

        public char get(int x, int y) {
            return map[y][x];
        }

        public char get(Coordinate coordinate) {
            return map[coordinate.y][coordinate.x];
        }

        public void set(Coordinate coordinate, char ch) {
            map[coordinate.y][coordinate.x] = ch;
        }

        public void set(Coordinate coordinate, Pipe pipe) {
            map[coordinate.y][coordinate.x] = pipe.getTile();
        }

        public boolean notInRange(Coordinate coordinate) {
            return coordinate.y < 0 || coordinate.y >= map.length
                || coordinate.x < 0 || coordinate.x >= map[0].length;
        }

        public Maze scaleUp(int factor) {
            char[][] scaledMap = new char[map.length * factor][map[0].length * factor];
            for (char[] row : scaledMap) {
                Arrays.fill(row, '.');
            }
            return Maze.of(start.scaleUp(factor), scaledMap);
        }

        public void print() {
            for (char[] row : map) {
                System.out.println(row);
            }

        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum Pipe {

        VERTICAL('|', List.of(Direction.NORTH, Direction.SOUTH), List.of(Tile.of(0, 0, '║'), Tile.of(0, 1, '║'))),
        HORIZONTAL_PIPE('-', List.of(Direction.EAST, Direction.WEST), List.of(Tile.of(0, 0, '═'), Tile.of(1, 0, '═'))),
        NINETY_DEGREE_NORTH_EAST('L', List.of(Direction.NORTH, Direction.EAST), List.of(Tile.of(0, -1, '║'), Tile.of(0, 0, '╚'), Tile.of(1, 0, '═'))),
        NINETY_DEGREE_NORTH_WEST('J', List.of(Direction.NORTH, Direction.WEST), List.of(Tile.of(0, -1, '║'), Tile.of(0, 0, '╝'), Tile.of(-1, 0, '═'))),
        NINETY_DEGREE_SOUTH_WEST('7', List.of(Direction.SOUTH, Direction.WEST), List.of(Tile.of(-1, 0, '═'), Tile.of(0, 0, '╗'), Tile.of(0, 1, '║'))),
        NINETY_DEGREE_SOUTH_EAST('F', List.of(Direction.SOUTH, Direction.EAST), List.of(Tile.of(1, 0, '═'), Tile.of(0, 0, '╔'), Tile.of(0, 1, '║')));

        private final char tile;
        private final List<Direction> nextDirections;
        private final List<Tile> tiles;

        private static final Map<Character, Pipe> REVERSE_LOOKUP_MAP = EnumUtils.toReverseLookupMap(Pipe.class, Pipe::getTile);

        public static Pipe fromTile(char label) {
            return REVERSE_LOOKUP_MAP.get(label);
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    private static class Tile extends Coordinate {

        private final char tile;

        Tile(int x, int y, char tile) {
            super(x, y);
            this.tile = tile;
        }

        public static Tile of(int x, int y, char tile) {
            return new Tile(x, y, tile);
        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum Direction {

        NORTH(Coordinate.of(0, -1)),
        EAST(Coordinate.of(1, 0)),
        SOUTH(Coordinate.of(0, 1)),
        WEST(Coordinate.of(-1, 0));

        private final Coordinate coordinate;

        public boolean isOpposite(Direction other) {
            return Math.abs(other.ordinal() - this.ordinal()) == 2;
        }
    }

    @SneakyThrows
    private Maze parseInput() {
        File file = ResourceUtils.getResourceFile("day10.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            int startX = 0;
            int startY = 0;

            List<char[]> map = new ArrayList<>();
            int x = -1;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (x == -1) {
                    if ((x = line.indexOf('S')) != -1) {
                        startX = x;
                    } else {
                        startY++;
                    }
                }
                map.add(line.toCharArray());
            }
            return Maze.of(Coordinate.of(startX, startY), map.toArray(new char[0][]));
        }
    }
}
