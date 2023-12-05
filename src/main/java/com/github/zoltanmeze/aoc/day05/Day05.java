package com.github.zoltanmeze.aoc.day05;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Day05 implements Runnable {

    public static void main(String[] args) {
        new Day05().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        Input input = parseInput();

        String current = "seed";
        String target = "location";

        long[] results = Arrays.copyOf(input.seeds, input.seeds.length);

        while (!current.equals(target)) {
            var sourceToDestinationMappings = input.maps.get(current);
            for (int i = 0; i < results.length; i++) {
                for (var mapping : sourceToDestinationMappings.mappings) {
                    if (mapping.sourceRange.isInRange(results[i])) {
                        var distance = mapping.sourceRange.getDistanceFromStart(results[i]);
                        results[i] = mapping.destinationRange.start + distance;
                        break; // no overlap between ranges, no need to iterate over every range and push to new list
                    }
                }
            }
            current = sourceToDestinationMappings.destination;
        }
        return Arrays.stream(results)
            .reduce(Math::min)
            .orElseThrow();
    }


    public Object partTwo() {
        Input input = parseInput();

        Queue<Range> currentSourceRanges = new ArrayDeque<>(input.seeds.length / 2);
        for (int i = 0; i < input.seeds.length - 1; i += 2) {
            currentSourceRanges.offer(Range.of(input.seeds[i], input.seeds[i] + input.seeds[i + 1] - 1));
        }

        String current = "seed";
        String target = "location";

        while (!current.equals(target)) {
            var sourceToDestinationMappings = input.maps.get(current);

            List<Range> sourceRanges = new ArrayList<>();
            List<Range> destinationRanges = new ArrayList<>();

            for (var mapping : sourceToDestinationMappings.mappings) {
                while (!currentSourceRanges.isEmpty()) {
                    Range currentSourceRange = currentSourceRanges.poll();
                    Range overlappingRange = mapping.sourceRange.getOverlappingRange(currentSourceRange);

                    if (overlappingRange == null) {
                        sourceRanges.add(currentSourceRange);
                        continue;
                    }
                    destinationRanges.add(mapping.calculateDestination(overlappingRange));
                    if (currentSourceRange.start < overlappingRange.start) {
                        sourceRanges.add(Range.of(currentSourceRange.start, overlappingRange.start - 1));
                    }
                    if (currentSourceRange.end > overlappingRange.end) {
                        sourceRanges.add(Range.of(overlappingRange.end + 1, currentSourceRange.end));
                    }
                }
                currentSourceRanges.addAll(sourceRanges);
                sourceRanges.clear();
            }
            currentSourceRanges.addAll(destinationRanges);
            destinationRanges.clear();
            current = sourceToDestinationMappings.destination;
        }
        return currentSourceRanges.stream()
            .mapToLong(Range::getStart)
            .reduce(Math::min)
            .orElseThrow();
    }

    @Data(staticConstructor = "of")
    private static class Input {
        private final long[] seeds;
        private final Map<String, SourceToDestinationMappings> maps;
    }

    @Data(staticConstructor = "of")
    private static class SourceToDestinationMappings {
        private final String destination;
        private final SourceToDestinationMapping[] mappings;
    }

    @Data(staticConstructor = "of")
    private static class Range {

        private final long start;
        private final long end;

        private boolean isInRange(long target) {
            return start <= target && target <= end;
        }

        private long getDistanceFromStart(long target) {
            return target - start;
        }

        private Range getOverlappingRange(Range other) {
            if (other.start > this.end || other.end < this.start) {
                return null;
            }
            long start = Math.max(this.start, other.start);
            long end = Math.min(this.end, other.end);
            return Range.of(start, end);
        }

        @Override
        public String toString() {
            return "[" + start + ", " + end + "]";
        }
    }

    @Data(staticConstructor = "of")
    private static class SourceToDestinationMapping {

        private final Range sourceRange;
        private final Range destinationRange;

        public static SourceToDestinationMapping of(long sourceRangeStart, long destinationRangeStart, long length) {
            return new SourceToDestinationMapping(
                Range.of(sourceRangeStart, sourceRangeStart + length - 1),
                Range.of(destinationRangeStart, destinationRangeStart + length - 1)
            );
        }

        public Range calculateDestination(Range overlap) {
            long start = sourceRange.getDistanceFromStart(overlap.start) + destinationRange.start;
            long end = overlap.getDistanceFromStart(overlap.end) + start;
            return Range.of(start, end);
        }
    }

    @SneakyThrows
    private Input parseInput() {
        File file = ResourceUtils.getResourceFile("day05.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            scanner.useDelimiter("\n\n");

            Pattern seedPattern = Pattern.compile("^seeds:\\h(?<SEEDS>(?:\\d+\\h?)*$)");
            Pattern mappingPattern = Pattern.compile("(?<SOURCE>\\w+)-to-(?<DESTINATION>\\w+)\\smap:\\n(?<MAP>(?:.*\\n?)*)");
            Pattern rangePattern = Pattern.compile("^(?<DESTINATION>\\d+)\\s(?<SOURCE>\\d+)\\s(?<LENGTH>\\d+)$");

            long[] seeds = null;
            if (scanner.hasNext()) {
                Matcher matcher = seedPattern.matcher(scanner.next());
                if (matcher.find()) {
                    seeds = Arrays.stream(matcher.group("SEEDS").split("\\h"))
                        .mapToLong(Long::parseLong)
                        .toArray();
                }
            }
            if (seeds == null) {
                throw new RuntimeException();
            }
            Input input = new Input(seeds, new HashMap<>());
            while (scanner.hasNext()) {
                String s = scanner.next();
                Matcher matcher = mappingPattern.matcher(s);
                if (!matcher.find()) {
                    throw new RuntimeException();
                }
                String source = matcher.group("SOURCE");
                String destination = matcher.group("DESTINATION");
                List<SourceToDestinationMapping> ranges = new ArrayList<>();
                try (Scanner scanner1 = new Scanner(matcher.group("MAP"))) {
                    while (scanner1.hasNextLine()) {
                        matcher = rangePattern.matcher(scanner1.nextLine());
                        if (!matcher.find()) {
                            throw new RuntimeException();
                        }
                        ranges.add(SourceToDestinationMapping.of(
                            Long.parseLong(matcher.group("SOURCE")),
                            Long.parseLong(matcher.group("DESTINATION")),
                            Long.parseLong(matcher.group("LENGTH"))
                        ));
                    }
                }
                input.maps.put(source, SourceToDestinationMappings.of(destination, ranges.toArray(new SourceToDestinationMapping[0])));
            }
            return input;
        }
    }
}
