package com.github.zoltanmeze.aoc.day15;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class Day15 implements Runnable {

    public static void main(String[] args) {
        new Day15().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        List<String> input = parseInput();

        return input.stream()
            .mapToInt(this::calculateHash)
            .reduce(0, Math::addExact);
    }

    public Object partTwo() {
        List<String> input = parseInput();

        Map<Integer, LinkedHashMap<String, Integer>> boxes = new HashMap<>(256);

        for (String step : input) {
            if (step.endsWith("-")) {
                String label = step.substring(0, step.length() - 1);
                boxes.computeIfPresent(calculateHash(label), (hash, lenses) -> {
                    if (lenses.remove(label) != null && lenses.isEmpty()) {
                        return null;
                    }
                    return lenses;
                });
            } else {
                int splitIndex = step.lastIndexOf('=');
                String label = step.substring(0, splitIndex);
                int focalLength = Integer.parseInt(step.substring(splitIndex + 1));
                boxes.computeIfAbsent(calculateHash(label), hash -> new LinkedHashMap<>()).put(label, focalLength); // Not changing order if already exist
            }
        }
        int sum = 0;
        for (var box : boxes.entrySet()) {
            int boxNumber = box.getKey() + 1;
            int slotNumber = 1;
            for (var lens : box.getValue().entrySet()) {
                sum += boxNumber * slotNumber++ * lens.getValue();
            }
        }
        return sum;
    }

    private int calculateHash(String string) {
        int code = 0;
        for (int i : string.toCharArray()) {
            code = (code + i) * 17 % 256;
        }
        return code;
    }

    @SneakyThrows
    private List<String> parseInput() {
        File file = ResourceUtils.getResourceFile("day15.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            scanner.useDelimiter(",");

            List<String> sequence = new ArrayList<>();
            while (scanner.hasNext()) {
                sequence.add(scanner.next().trim());
            }
            return sequence;
        }
    }
}
