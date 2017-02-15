package no.obos.util.servicebuilder.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.stream.Collector;

public class GuavaHelper {
    public static <T> ImmutableList<T> plus(ImmutableList<T> list, T element) {
        return ImmutableList.<T>builder()
                .addAll(list)
                .add(element)
                .build();
    }

    public static <K, V> ImmutableMap<K, V> plus(ImmutableMap<K, V> map, K key, V value) {
        return ImmutableMap.<K, V>builder()
                .putAll(map)
                .put(key, value)
                .build();
    }

    public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> listCollector() {
        return Collector.of(ImmutableList.Builder<T>::new, ImmutableList.Builder<T>::add, (l, r) -> l.addAll(r.build()), ImmutableList.Builder<T>::build);
    }
}
