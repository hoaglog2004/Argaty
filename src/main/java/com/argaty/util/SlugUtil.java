package com.argaty.util;

import com.github.slugify.Slugify;

/**
 * Utility class để tạo slug từ text (hỗ trợ tiếng Việt)
 */
public class SlugUtil {

    private static final Slugify slugify = Slugify.builder()
            .lowerCase(true)
            .build();

    /**
     * Tạo slug từ text
     * VD: "Bàn phím cơ Gaming" -> "ban-phim-co-gaming"
     */
    public static String toSlug(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        return slugify.slugify(text);
    }

    /**
     * Tạo slug unique bằng cách thêm suffix
     */
    public static String toUniqueSlug(String text, String suffix) {
        String baseSlug = toSlug(text);
        if (suffix != null && !suffix.isEmpty()) {
            return baseSlug + "-" + suffix;
        }
        return baseSlug;
    }
}