package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Dependency {
    public final Class<?> depender;
    public final Class<?> dependentOn;
}
