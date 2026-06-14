package com.cogniflow.util;

import com.cogniflow.entity.StudyRecord;
import java.util.*;

public class CollectionUtil {

    public static <T extends Comparable<T>> List<T> sort(List<T> list) {
        List<T> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        return sorted;
    }

    public static List<StudyRecord> getFailedRecords(List<StudyRecord> records) {
        List<StudyRecord> failed = new ArrayList<>();
        for (StudyRecord r : records) {
            if ("FAILED".equals(r.getStatus()) || (r.getScore() != null && r.getScore() < 60)) {
                failed.add(r);
            }
        }
        return failed;
    }

    public static Map<String, List<StudyRecord>> groupByStatus(List<StudyRecord> records) {
        Map<String, List<StudyRecord>> map = new HashMap<>();
        for (StudyRecord r : records) {
            String key = r.getStatus() != null ? r.getStatus() : "未知";
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }
        return map;
    }
}
