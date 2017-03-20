package no.obos.util.servicebuilder.util;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FormatUtil {
    public static Function<String, String> indentLine(String indentation) {
        return line -> indentation + line;
    }

    public static List<String> indentLines(List<String> lines, String indentation) {
        return lines.stream()
                .map(indentLine(indentation))
                .collect(Collectors.toList());
    }

    public static List<String> stringMapAsIndentedLines(Map<String, String> stringMap, Set<String> skipKeys, String indentation) {
        return stringMap.entrySet().stream()
                .filter(entry -> ! skipKeys.contains(entry.getKey()))
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .map(indentLine(indentation))
                .collect(Collectors.toList());
    }

    public static Map<String, String> MultiMapAsStringMap(MultivaluedMap<String, String> multiMap) {
        return multiMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<String> values = entry.getValue();
                            if (values == null || values.isEmpty()) {
                                return "null";
                            } else if (values.size() == 1) {
                                return values.get(0);
                            } else {
                                return values.toString();
                            }
                        }
                ));
    }
}
