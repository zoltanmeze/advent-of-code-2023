package com.github.zoltanmeze.aoc.day08;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Day08 implements Runnable {

    public static void main(String[] args) {
        new Day08().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        Input input = parseInput();

        String current = "AAA";
        String target = "ZZZ";

        return findClosestNode(current, 0, input, l -> l.equals(target)).steps;
    }

    public Object partTwo() {
        Input input = parseInput();

        var endsWithZ = new Predicate<String>() {
            @Override
            public boolean test(String label) {
                return label.endsWith("Z");
            }
        };

        long results = 1;
        for (String label : input.nodes.keySet()) {
            if (!endsWithZ.test(label)) {
                continue;
            }
            var closestNodeFromA = findClosestNode(label, 0, input, endsWithZ);
            var closestNodeFromZ = findClosestNode(closestNodeFromA.label, (int) (closestNodeFromA.steps % input.instructions.length), input, endsWithZ);

            if (!closestNodeFromA.equals(closestNodeFromZ)) {
                // Nodes from Z always loops around with same number of steps
                // Otherwise we would need to look further and/or combine two methods
                throw new RuntimeException("Not looping around with same number of steps");
            }
            results = lcm(results, closestNodeFromA.steps);
        }
        return results;
    }

    private ClosestNode findClosestNode(String fromLabel, int fromIndex, Input input, Predicate<String> predicate) {
        String label = fromLabel;
        int index = fromIndex;

        long steps = 0;
        do {
            Node instruction = input.nodes.get(label);
            label = input.instructions[index] == 'L' ? instruction.left : instruction.right;
            index = (index + 1) % input.instructions.length;
            steps++;
        } while (!predicate.test(label));
        return ClosestNode.of(steps, label);
    }

    private long lcm(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        } else {
            long gcd = gcd(a, b);
            return Math.abs(a * b) / gcd;
        }
    }

    private long gcd(long a, long b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    @Data(staticConstructor = "of")
    private static class ClosestNode {
        private final long steps;
        private final String label;
    }

    @Data(staticConstructor = "of")
    private static class Input {
        private final char[] instructions;
        private final Map<String, Node> nodes;
    }

    @Data(staticConstructor = "of")
    private static class Node {
        private final String left;
        private final String right;
    }

    @SneakyThrows
    private Input parseInput() {
        File file = ResourceUtils.getResourceFile("day08.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            Pattern pattern = Pattern.compile("^(?<LABEL>\\w++)\\s+=\\s+\\((?<LEFT>\\w+),\\s+(?<RIGHT>\\w+)\\)$");
            if (!scanner.hasNextLine()) {
                throw new RuntimeException();
            }
            char[] instructions = scanner.nextLine().toCharArray();
            Map<String, Node> nodes = new HashMap<>();
            while (scanner.hasNextLine()) {
                Matcher matcher = pattern.matcher(scanner.nextLine());
                if (matcher.find()) {
                    String label = matcher.group("LABEL");
                    var node = nodes.put(label, Node.of(matcher.group("LEFT"), matcher.group("RIGHT")));
                    if (node != null) {
                        throw new RuntimeException("Node with label " + label + " already exist");
                    }
                }
            }
            return Input.of(instructions, nodes);
        }
    }
}
