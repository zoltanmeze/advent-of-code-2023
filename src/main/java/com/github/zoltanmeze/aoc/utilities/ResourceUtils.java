package com.github.zoltanmeze.aoc.utilities;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.net.URL;

@UtilityClass
public class ResourceUtils {

    public static File getResourceFile(String name) {
        URL resourceURL = ResourceUtils.class.getClassLoader().getResource(name);
        if (resourceURL == null) {
            throw new RuntimeException("Resource file at location " + name + " not found");
        }
        return new File(resourceURL.getPath());
    }
}
