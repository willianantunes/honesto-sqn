package br.com.willianantunes.util;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class HttpUtils {

    public static Map<String, List<String>> splitQuery(URL url) {

        if (url.getQuery() == null || url.getQuery().isEmpty()) {

            return Collections.emptyMap();
        }

        return Arrays.stream(url.getQuery().split("&"))
            .map(HttpUtils::splitQueryParameter)
            .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, LinkedHashMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private static AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {

        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}