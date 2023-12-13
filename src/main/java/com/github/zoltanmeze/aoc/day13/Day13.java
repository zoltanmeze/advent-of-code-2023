package com.github.zoltanmeze.aoc.day13;

import com.github.zoltanmeze.aoc.utilities.ResourceUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Slf4j
public class Day13 implements Runnable {

    public static void main(String[] args) {
        new Day13().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        List<char[][]> input = parseInput();

        long result = 0;
        int symbolsToChange = 0;

        for (char[][] terrain : input) {
            int columnsLeft = calculate(terrain, symbolsToChange);
            if (columnsLeft == 0) {
                char[][] flippedTerrain = reverse(terrain);
                var columnsRight = calculate(flippedTerrain, symbolsToChange);
                if (columnsRight > 0) {
                    columnsLeft = flippedTerrain.length - columnsRight;
                }
            }

            char[][] rotatedTerrain = transpose(terrain);
            int rowsAbove = calculate(rotatedTerrain, symbolsToChange);
            if (rowsAbove == 0) {
                char[][] flippedRotatedTerrain = reverse(rotatedTerrain);
                var rowsBelow = calculate(flippedRotatedTerrain, symbolsToChange);
                if (rowsBelow > 0) {
                    rowsAbove = flippedRotatedTerrain.length - rowsBelow;
                }
            }
            result += rowsAbove + 100L * columnsLeft;
        }
        return result;
    }

    public Object partTwo() {
        List<char[][]> input = parseInput();

        long result = 0;
        int symbolsToChange = 1;

        for (char[][] terrain : input) {
            int columnsLeft = calculate(terrain, symbolsToChange);
            if (columnsLeft == 0) {
                char[][] flippedTerrain = reverse(terrain);
                var columnsRight = calculate(flippedTerrain, symbolsToChange);
                if (columnsRight > 0) {
                    columnsLeft = flippedTerrain.length - columnsRight;
                }
            }

            char[][] rotatedTerrain = transpose(terrain);
            int rowsAbove = calculate(rotatedTerrain, symbolsToChange);
            if (rowsAbove == 0) {
                char[][] flippedRotatedTerrain = reverse(rotatedTerrain);
                var rowsBelow = calculate(flippedRotatedTerrain, symbolsToChange);
                if (rowsBelow > 0) {
                    rowsAbove = flippedRotatedTerrain.length - rowsBelow;
                }
            }
            result += rowsAbove + 100L * columnsLeft;
        }
        return result;
    }

    /**
     * Finds number of rows above vertically mirrored terrain.
     * Reflection needs to include last row.
     */
    private int calculate(char[][] terrain, int toChange) {
        for (int i = 0; i < terrain.length - 1; i++) {
            int k = (terrain.length - i - 1) / 2;
            int changed = 0;
            boolean found = true;
            for (int j = terrain.length - 1; k >= 0; k--) {
                if (i + k == j - k || (changed += lazyHammingDistance(terrain[i + k], terrain[j - k], toChange - changed)) > toChange) {
                    found = false;
                    break;
                }
            }
            if (found && changed == toChange) {
                return i + ((terrain.length - i) / 2);
            }
        }
        return 0;
    }

    private char[][] reverse(char[][] chs) {
        char[][] copy = new char[chs.length][chs[0].length];
        for (int i = 0; i < chs.length; i++) {
            System.arraycopy(chs[i], 0, copy[chs.length - 1 - i], 0, chs[0].length);
        }
        return copy;
    }

    private char[][] transpose(char[][] chs) {
        char[][] copy = new char[chs[0].length][chs.length];
        for (int i = 0; i < chs.length; i++) {
            for (int j = 0; j < chs[0].length; j++) {
                copy[j][i] = chs[i][j];
            }
        }
        return copy;
    }

    private int lazyHammingDistance(char[] first, char[] second, int maxAllowed) {
        int count = 0;
        for (int i = 0; i < first.length; i++) {
            if (first[i] != second[i] && (++count > maxAllowed)) {
                break;
            }
        }
        return count;
    }

    @SneakyThrows
    private List<char[][]> parseInput() {
        File file = ResourceUtils.getResourceFile("day13.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            List<char[][]> terrains = new ArrayList<>();
            scanner.useDelimiter("\\n\\n");
            while (scanner.hasNext()) {
                try (Scanner scanner1 = new Scanner(scanner.next())) {
                    List<char[]> terrain = new ArrayList<>();
                    while (scanner1.hasNextLine()) {
                        terrain.add(scanner1.nextLine().toCharArray());
                    }
                    terrains.add(terrain.toArray(new char[0][]));
                }
            }
            return terrains;
        }
    }
}
