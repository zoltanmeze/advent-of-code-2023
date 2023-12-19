package com.github.zoltanmeze.aoc.day19;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Day19 implements Runnable {

    public static void main(String[] args) {
        new Day19().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        PartOrganizerSystem input = parseInput();
        long sum = 0L;
        for (Part part : input.parts) {
            String workflow = "in";
            do {
                for (Rule rule : input.workflows.get(workflow)) {
                    if (rule.test(part)) {
                        workflow = rule.destination;
                        break;
                    }
                }
            } while (!workflow.equals("A") && !workflow.equals("R"));

            if (workflow.equals("A")) {
                sum += part.sum();
            }
        }
        return sum;
    }

    private Object partTwo() {
        PartOrganizerSystem input = parseInput();

        Stack<State> stack = new Stack<>();
        stack.add(State.of(Part.of(1, 1, 1, 1), Part.of(4000, 4000, 4000, 4000), "in"));

        long combinations = 0;
        do {
            State state = stack.pop();
            if (state.workflow.equals("A")) {
                combinations += state.combinations();
                continue;
            } else if (state.workflow.equals("R")) {
                continue;
            }

            Part minPart = state.minPart;
            Part maxPart = state.maxPart;

            for (Rule rule : input.workflows.get(state.workflow)) {
                Part newMinPart = minPart.copy();
                Part newMaxPart = maxPart.copy();
                if (rule.operator == Operator.GREATER) {
                    newMinPart.set(rule.attribute, Math.max(rule.number + 1, newMinPart.get(rule.attribute)));
                    maxPart.set(rule.attribute, Math.min(rule.number, maxPart.get(rule.attribute)));
                } else if (rule.operator == Operator.LESSER) {
                    newMaxPart.set(rule.attribute, Math.min(rule.number - 1, newMaxPart.get(rule.attribute)));
                    minPart.set(rule.attribute, Math.max(rule.number, minPart.get(rule.attribute)));
                }
                stack.push(State.of(newMinPart, newMaxPart, rule.destination));
            }
        } while (!stack.isEmpty());
        return combinations;
    }

    @Data(staticConstructor = "of")
    private static class State {

        private final Part minPart;
        private final Part maxPart;
        private final String workflow;

        public long combinations() {
            long product = 1;
            for (int i = 0; i < maxPart.values.length; i++) {
                product *= maxPart.values[i] - minPart.values[i] + 1;
            }
            return product;
        }
    }

    @Data(staticConstructor = "of")
    private static class Rule implements Predicate<Part> {

        private final String destination;
        private final Character attribute; // operand1
        private final Operator operator;
        private final Integer number; // operand2

        @Override
        public boolean test(Part part) {
            if (operator == null) {
                return true;
            }
            return operator.test(part.get(attribute), number);
        }
    }

    @Data
    private static class Part {

        private static final Map<Character, Integer> ATTRIBUTES_MAP = Map.of('x', 0, 'm', 1, 'a', 2, 's', 3);

        private final int[] values;

        private Part(int[] values) {
            this.values = values;
        }

        public static Part of(int x, int m, int a, int s) {
            return new Part(new int[] {x, m, a, s});
        }

        public int get(char attribute) {
            return values[ATTRIBUTES_MAP.get(attribute)];
        }

        private void set(char attribute, int value) {
            values[ATTRIBUTES_MAP.get(attribute)] = value;
        }

        private long sum() {
            long sum = 0;
            for (int val : values) {
                sum += val;
            }
            return sum;
        }

        protected Part copy() {
            return new Part(Arrays.copyOf(values, values.length));
        }
    }

    @RequiredArgsConstructor
    private enum Operator implements BiPredicate<Integer, Integer> {

        GREATER('>', (a, b) -> a > b),
        LESSER('<', (a, b) -> a < b);

        @Getter
        private final char label;
        private final BiPredicate<Integer, Integer> predicate;

        private static final Map<Character, Operator> REVERSE_LOOKUP_MAP = EnumUtils.toReverseLookupMap(Operator.class, Operator::getLabel);

        public static Operator fromLabel(char label) {
            return REVERSE_LOOKUP_MAP.get(label);
        }

        @Override
        public boolean test(Integer integer, Integer integer2) {
            return predicate.test(integer, integer2);
        }
    }

    @Data(staticConstructor = "of")
    private static class PartOrganizerSystem {

        private final Map<String, List<Rule>> workflows;
        private final List<Part> parts;

    }

    @SneakyThrows
    private PartOrganizerSystem parseInput() {
        File file = ResourceUtils.getResourceFile("day19.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            scanner.useDelimiter("\\n\\n");

            Pattern workflowPattern = Pattern.compile("^(?<WORKFLOW>\\w+)\\{(?<RULES>.*)}$");
            Pattern rulePattern = Pattern.compile("^(?<CONDITION>(?<OPERAND1>[xsam])(?<OPERATOR>[<>])(?<OPERAND2>\\d+):)?(?<DESTINATION>[a-z]+|A|R)$");
            Pattern partPattern = Pattern.compile("^\\{x=(?<X>\\d+),m=(?<M>\\d+),a=(?<A>\\d+),s=(?<S>\\d+)}$");

            if (!scanner.hasNext()) {
                throw new RuntimeException();
            }
            Map<String, List<Rule>> workflows = new HashMap<>();
            try (Scanner scanner1 = new Scanner(scanner.next())) {
                while (scanner1.hasNextLine()) {
                    Matcher matcher = workflowPattern.matcher(scanner1.nextLine());
                    if (!matcher.matches()) {
                        throw new RuntimeException();
                    }
                    String workflow = matcher.group("WORKFLOW");
                    List<Rule> rulesList = new ArrayList<>();
                    for (String rule : matcher.group("RULES").split(",")) {
                        matcher = rulePattern.matcher(rule);
                        if (!matcher.matches()) {
                            throw new RuntimeException();
                        }
                        String destination = matcher.group("DESTINATION");
                        if (matcher.group("CONDITION") == null) {
                            rulesList.add(Rule.of(destination, null, null, null));
                        } else {
                            var operand1 = matcher.group("OPERAND1").charAt(0);
                            var operator = Operator.fromLabel(matcher.group("OPERATOR").charAt(0));
                            var operand2 = Integer.parseInt(matcher.group("OPERAND2"));
                            rulesList.add(Rule.of(destination, operand1, operator, operand2));
                        }
                    }
                    workflows.put(workflow, rulesList);
                }
            }
            if (!scanner.hasNext()) {
                throw new RuntimeException();
            }
            List<Part> parts = new ArrayList<>();
            try (Scanner scanner1 = new Scanner(scanner.next())) {
                while (scanner1.hasNextLine()) {
                    Matcher matcher = partPattern.matcher(scanner1.nextLine());
                    if (!matcher.matches()) {
                        throw new RuntimeException();
                    }
                    parts.add(Part.of(
                        Integer.parseInt(matcher.group("X")), Integer.parseInt(matcher.group("M")),
                        Integer.parseInt(matcher.group("A")), Integer.parseInt(matcher.group("S"))
                    ));
                }
            }

            return PartOrganizerSystem.of(workflows, parts);
        }
    }
}
