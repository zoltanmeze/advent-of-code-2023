package com.github.zoltanmeze.aoc.day07;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

@Slf4j
public class Day07 implements Runnable {

    public static void main(String[] args) {
        new Day07().run();
    }

    @Override
    public void run() {
        log.info("Part one: {}", partOne());
        log.info("Part two: {}", partTwo());
    }

    public Object partOne() {
        List<Hand> input = parseInput();

        final int[] rank = {input.size()};

        Map<Hand, HandType> cachedHandTypes = new HashMap<>(input.size());

        Comparator<Hand> comparator = Comparator.comparing((Hand hand) ->
                cachedHandTypes.computeIfAbsent(hand, h -> h.getHandType(false)))
            .thenComparing(Hand::getCards, Arrays::compare);

        return input.stream()
            .sorted(comparator)
            .mapToLong(Hand::getBid)
            .map(x -> rank[0]-- * x)
            .reduce(0L, Long::sum);
    }

    public Object partTwo() {
        List<Hand> input = parseInput();

        final int[] rank = {input.size()};

        Map<Hand, HandType> cachedHandTypes = new HashMap<>(input.size());

        Comparator<Hand> comparator = Comparator.comparing((Hand hand) ->
                cachedHandTypes.computeIfAbsent(hand, h -> h.getHandType(true)))
            .thenComparing(Hand::getCards, (cards1, cards2) ->
                Arrays.compare(cards1, cards2, (card1, card2) -> {
                    var ordinal1 = card1 == Card.JACK ? Integer.MAX_VALUE : card1.ordinal();
                    var ordinal2 = card2 == Card.JACK ? Integer.MAX_VALUE : card2.ordinal();
                    return Integer.compare(ordinal1, ordinal2);
                }));

        return input.stream()
            .sorted(comparator)
            .mapToLong(Hand::getBid)
            .map(x -> rank[0]-- * x)
            .reduce(0L, Long::sum);
    }

    @Data(staticConstructor = "of")
    private static class Hand {

        private final Card[] cards;
        private final int bid;

        private HandType getHandType(boolean treatJackAsJoker) {
            int[] indexes = new int[cards.length];
            int[] frequencies = new int[Card.values().length];

            int lastIndex = 0;
            int jokers = 0;

            for (Card card : cards) {
                if (treatJackAsJoker && card == Card.JACK) {
                    jokers++;
                    continue;
                }
                if (frequencies[card.ordinal()]++ == 0) {
                    indexes[lastIndex++] = card.ordinal();
                }
            }
            char[] ch = new char[Math.max(lastIndex, 1)];
            for (int i = 0; i < lastIndex - 1; i++) {
                for (int j = i + 1; j < lastIndex; j++) {
                    if (frequencies[indexes[i]] < frequencies[indexes[j]]) {
                        var temp = indexes[i];
                        indexes[i] = indexes[j];
                        indexes[j] = temp;
                    }
                }
                ch[i] = (char) (frequencies[indexes[i]] + '0');
            }
            lastIndex = Math.max(0, lastIndex - 1);
            ch[lastIndex] = (char) (frequencies[indexes[lastIndex]] + '0');
            ch[0] += (char) jokers;

            return Optional.ofNullable(HandType.fromFrequencies(new String(ch)))
                .orElseThrow(RuntimeException::new);
        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum HandType {

        FIVE_OF_A_KIND("5"),
        FOUR_OF_A_KIND("41"),
        FULL_HOUSE("32"),
        THREE_OF_A_KIND("311"),
        TWO_PAIR("221"),
        ONE_PAIR("2111"),
        HIGH_CARD("11111");

        private final String frequencies;

        private static final Map<String, HandType> REVERSE_LOOKUP_MAP = EnumUtils.toReverseLookupMap(HandType.class, HandType::getFrequencies);

        public static HandType fromFrequencies(String frequencies) {
            return REVERSE_LOOKUP_MAP.get(frequencies);
        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum Card implements Comparable<Card> {

        ACE('A'),
        KING('K'),
        QUEEN('Q'),
        JACK('J'),
        TEN('T'),
        NINE('9'),
        EIGHT('8'),
        SEVEN('7'),
        SIX('6'),
        FIVE('5'),
        FOUR('4'),
        THREE('3'),
        TWO('2');

        private final char label;

        private static final Map<Character, Card> REVERSE_LOOKUP_MAP = EnumUtils.toReverseLookupMap(Card.class, Card::getLabel);

        public static Card fromLabel(char label) {
            return REVERSE_LOOKUP_MAP.get(label);
        }

        @Override
        public String toString() {
            return Character.toString(label);
        }
    }

    @SneakyThrows
    private List<Hand> parseInput() {
        File file = ResourceUtils.getResourceFile("day07.txt");
        try (
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader)
        ) {
            List<Hand> hands = new ArrayList<>();
            while (scanner.hasNextLine()) {
                var line = scanner.nextLine().split("\\s+");
                var cards = line[0].chars()
                    .mapToObj(x -> (char) x)
                    .map(Card::fromLabel)
                    .toArray(Card[]::new);
                var bid = Integer.parseInt(line[1]);
                hands.add(Hand.of(cards, bid));
            }
            return hands;
        }
    }
}
