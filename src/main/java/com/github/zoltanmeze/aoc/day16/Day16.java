package com.github.zoltanmeze.aoc.day16;

import com.github.zoltanmeze.aoc.utilities.EnumUtils;
import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

@Slf4j
public class Day16 implements Runnable {

    public static void main(String[] args) {
        new Day16().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        Tile[][] input = parseInput();
        Position start = Position.of(0, 0, Direction.RIGHT);

        return countEnergizedTiles(input, start);
    }

    public Object partTwo() {
        Tile[][] input = parseInput();

        assert input.length == input[0].length;

        Set<Position> startingPositions = new HashSet<>(4 * input.length);
        for (int i = 0; i < input.length; i++) {
            startingPositions.add(Position.of(0, i, Direction.RIGHT));
            startingPositions.add(Position.of(input[i].length - 1, i, Direction.LEFT));
            startingPositions.add(Position.of(i, 0, Direction.DOWN));
            startingPositions.add(Position.of(i, input.length - 1, Direction.UP));
        }

        return startingPositions.parallelStream()
            .mapToInt(position -> countEnergizedTiles(input, position))
            .reduce(Math::max)
            .orElseThrow();
    }

    private int countEnergizedTiles(Tile[][] grid, Position start) {
        byte[][] visited = new byte[grid.length][grid[0].length];
        int count = 0;

        Stack<Position> stack = new Stack<>();
        stack.push(start);
        do {
            Position position = stack.pop();
            if (position.y < 0 || position.x < 0
                || position.y >= grid.length || position.x >= grid[position.y].length
                || (visited[position.y][position.x] & position.direction.mask) > 0) {
                continue;
            } else if (visited[position.y][position.x] == 0) {
                count++;
            }
            visited[position.y][position.x] |= position.direction.mask; // Better than constantly shifting bits
            stack.addAll(position.move(grid));
        } while (!stack.isEmpty());
        return count;
    }

    @ToString
    @AllArgsConstructor(staticName = "of")
    private static class Position {

        private int x;
        private int y;
        private Direction direction;

        private List<Position> move(Tile[][] map) {
            Direction[] directions = map[y][x].getNext(direction);
            List<Position> next = new ArrayList<>(directions.length);
            for (Direction direction : directions) {
                next.add(move(direction));
            }
            return next;
        }

        private Position move(Direction direction) {
            return Position.of(this.x + direction.x, this.y + direction.y, direction);
        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum Direction {

        UP(0, -1, (byte) 1),
        LEFT(-1, 0, (byte) 2),
        DOWN(0, 1, (byte) 4),
        RIGHT(1, 0, (byte) 8);

        private final int x;
        private final int y;
        private final byte mask;
    }

    @Getter
    @RequiredArgsConstructor
    private enum Tile {

        EMPTY_SPACE('.', new Direction[][] {{Direction.UP}, {Direction.LEFT}, {Direction.DOWN}, {Direction.RIGHT}}),
        MIRROR_F('/', new Direction[][] {{Direction.RIGHT}, {Direction.DOWN}, {Direction.LEFT}, {Direction.UP}}),
        MIRROR_B('\\', new Direction[][] {{Direction.LEFT}, {Direction.UP}, {Direction.RIGHT}, {Direction.DOWN}}),
        SPLITTER_H('-', new Direction[][] {{Direction.LEFT, Direction.RIGHT}, {Direction.LEFT}, {Direction.LEFT, Direction.RIGHT}, {Direction.RIGHT}}),
        SPLITTER_V('|', new Direction[][] {{Direction.UP}, {Direction.UP, Direction.DOWN}, {Direction.DOWN}, {Direction.UP, Direction.DOWN}});

        private final char label;
        private final Direction[][] next;

        private static final Map<Character, Tile> LOOKUP_MAP = EnumUtils.toReverseLookupMap(Tile.class, Tile::getLabel);

        public static Tile fromLabel(char label) {
            return LOOKUP_MAP.get(label);
        }

        public Direction[] getNext(Direction current) {
            return next[current.ordinal()];
        }
    }

    @SneakyThrows
    private Tile[][] parseInput() {
        File file = ResourceUtils.getResourceFile("day16.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            List<Tile[]> grid = new ArrayList<>();
            while (scanner.hasNextLine()) {
                grid.add(scanner.nextLine()
                    .codePoints()
                    .mapToObj(x -> (char) x)
                    .map(Tile::fromLabel)
                    .toArray(Tile[]::new));
            }
            return grid.toArray(new Tile[0][]);
        }
    }
}
