package no.obos.util.servicebuilder.experimental;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Dependency {
    public final Class<?> depender;
    public final Class<?> dependentOn;
}
