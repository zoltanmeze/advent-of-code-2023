package com.github.zoltanmeze.aoc.utilities;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class EnumUtils {

    public static <T extends Enum<T>, R> Map<R, T> toReverseLookupMap(final Class<T> enumClass,
                                                                      final Function<T, R> keyMapper) {
        return Arrays.stream(enumClass.getEnumConstants())
            .collect(Collectors.toUnmodifiableMap(keyMapper, Function.identity()));
    }

}
