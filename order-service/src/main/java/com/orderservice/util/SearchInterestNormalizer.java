package com.orderservice.util;

public final class SearchInterestNormalizer {

    private SearchInterestNormalizer() {
    }

    public static String normalizeInterestKey(String query) {
        if (query == null) {
            return "";
        }
        return query.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    public static String extractCategoryLabel(String categoryPath) {
        if (categoryPath == null || categoryPath.isBlank()) {
            return null;
        }
        String[] segments = categoryPath.split("/");
        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i].trim();
            if (!segment.isEmpty() && !segment.equalsIgnoreCase("products")) {
                return segment;
            }
        }
        return categoryPath.trim();
    }
}
