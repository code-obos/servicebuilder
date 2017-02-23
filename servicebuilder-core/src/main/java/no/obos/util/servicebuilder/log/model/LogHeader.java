package no.obos.util.servicebuilder.log.model;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class LogHeader {
    public final String name;
    public final ImmutableList<String> values;
}
