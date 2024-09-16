package com.project.apex.util;

import java.util.ArrayList;
import java.util.List;

public class Quantities {

    public static List<Integer> divideIntoThreeGroups(int totalItems) {
        List<Integer> groupSizes = new ArrayList<>();

        int baseSize = totalItems / 3;  // Base size for each group
        int remainder = totalItems % 3; // Remaining items to distribute

        // Add the base size to all groups
        groupSizes.add(baseSize);
        groupSizes.add(baseSize);
        groupSizes.add(baseSize);

        // Distribute the remainder
        for (int i = 0; i < remainder; i++) {
            groupSizes.set(i, groupSizes.get(i) + 1);
        }

        return groupSizes;
    }
}
