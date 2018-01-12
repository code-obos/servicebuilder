package no.obos.util.servicebuilder.es.options;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class IndexingOptions {

    public static final IndexingOptions DEFAULT = IndexingOptions.builder()
            .bulkSize(2000)
            .bulkConcurrent(5)
            .build();

    int bulkSize;
    int bulkConcurrent;
}
