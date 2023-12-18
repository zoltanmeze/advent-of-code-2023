package com.github.zoltanmeze.aoc.day18;

import com.github.zoltanmeze.aoc.utilities.EnumUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Day18 implements Runnable {

    public static void main(String[] args) {
        new Day18().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        Plan[] input = parseInput();

        return calculatePolygonArea(input);
    }

    private Object partTwo() {
        Plan[] input = parseInput();

        Plan[] newPlan = new Plan[input.length];

        for (int i = 0; i < input.length; i++) {
            Plan plan = input[i];

            var length = Integer.parseInt(plan.color.substring(0, 5), 16); // Base 16 to base 10
            var direction = Direction.values()[plan.color.charAt(5) - '0'];

            newPlan[i] = Plan.of(direction, length, plan.color);
        }
        return calculatePolygonArea(newPlan);
    }

    private long calculatePolygonArea(Plan[] plans) {
        long area = 0;
        long perimeter = 0;
        Coordinate current = Coordinate.of(0, 0);

        Plan last = plans[plans.length - 1];
        current.move(last.direction, last.length);

        for (Plan plan : plans) {
            Coordinate next = current.move(plan.direction, plan.length);
            perimeter += plan.length; // manhattan distance between two points
            area += (long) current.x * next.y - (long) next.x * current.y;
            current = next;
        }
        return (Math.abs(area) + perimeter) / 2 + 1;
    }

    @Getter
    @RequiredArgsConstructor
    private enum Direction {

        RIGHT('R', Coordinate.of(1, 0)),
        DOWN('D', Coordinate.of(0, 1)),
        LEFT('L', Coordinate.of(-1, 0)),
        UP('U', Coordinate.of(0, -1));

        private final char label;
        private final Coordinate next;

        private static final Map<Character, Direction> REVERSE_LOOKUP_MAP = EnumUtils.toReverseLookupMap(Direction.class, Direction::getLabel);

        public static Direction fromLabel(char label) {
            return REVERSE_LOOKUP_MAP.get(label);
        }
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    private static class Coordinate {

        private final int x;
        private final int y;

        public Coordinate move(Direction direction, int distance) {
            return Coordinate.of(x + direction.next.x * distance, y + direction.next.y * distance);
        }
    }

    @Data(staticConstructor = "of")
    private static class Plan {
        private final Direction direction;
        private final int length;
        private final String color;
    }

    @SneakyThrows
    private Plan[] parseInput() {
        File file = ResourceUtils.getResourceFile("day18.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            List<Plan> plans = new ArrayList<>();
            Pattern patter = Pattern.compile("^(?<DIRECTION>[URDL])\\s(?<LENGTH>\\d+)\\s\\(#(?<COLOR>[0-9a-h]{6})\\)$");
            while (scanner.hasNextLine()) {
                Matcher matcher = patter.matcher(scanner.nextLine());
                if (!matcher.matches()) {
                    throw new RuntimeException();
                }
                plans.add(Plan.of(
                    Direction.fromLabel(matcher.group("DIRECTION").charAt(0)),
                    Integer.parseInt(matcher.group("LENGTH")),
                    matcher.group("COLOR")
                ));

            }
            return plans.toArray(new Plan[0]);
        }
    }
}
