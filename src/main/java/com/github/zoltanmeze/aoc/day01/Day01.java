package com.github.zoltanmeze.aoc.day01;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class Day01 implements Runnable {

    public static void main(String[] args) {
        new Day01().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        List<String> input = parseInput();

        int sum = 0;

        for (String line : input) {
            Integer first = null;
            Integer last = null;

            for (char ch : line.toCharArray()) {
                if (ch >= '0' && ch <= '9') {
                    int num = ch - '0';
                    if (first == null) {
                        first = num;
                    }
                    last = num;
                }
            }
            assert first != null;
            sum += 10 * first + last;
        }
        return sum;
    }


    public Object partTwo() {
        String[] numbers = new String[] {
            "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"
        };
        TrieNode trie = new TrieNode();
        for (int i = 0; i < numbers.length; i++) {
            trie.add(numbers[i], i + 1);
        }

        List<String> input = parseInput();

        int sum = 0;

        for (String line : input) {
            char[] chars = line.toCharArray();

            Integer first = null;
            Integer last = null;

            for (int i = 0; i < chars.length; i++) {
                char ch = chars[i];
                Integer num = null;

                if (ch >= '1' && ch <= '9') {
                    num = ch - '0';
                } else {
                    TrieNode currentNode = trie;
                    for (int j = i; j < chars.length; j++) {
                        currentNode = currentNode.getNext(chars[j]);
                        if (currentNode == null) {
                            break;
                        } else if (currentNode.isLeaf()) {
                            num = currentNode.getData();
                            break;
                        }
                    }
                }
                if (num != null) {
                    if (first == null) {
                        first = num;
                    }
                    last = num;
                }
            }
            assert first != null;
            sum += 10 * first + last;
        }

        return sum;
    }

    @Getter
    @NoArgsConstructor
    private static class TrieNode {

        private Integer data;
        private final Map<Character, TrieNode> children = new HashMap<>();

        public void add(final String string, final int index) {
            TrieNode current = this;
            for (char ch : string.toCharArray()) {
                current = current.children.computeIfAbsent(ch, k -> new TrieNode());
            }
            current.data = index;
        }

        public TrieNode getNext(final char ch) {
            return children.get(ch);
        }

        public boolean isLeaf() {
            return data != null;
        }
    }

    @SneakyThrows
    public List<String> parseInput() {
        File file = ResourceUtils.getResourceFile("day01.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            List<String> groups = new ArrayList<>();
            while (scanner.hasNext()) {
                groups.add(scanner.nextLine());
            }
            return groups;
        }
    }
}
