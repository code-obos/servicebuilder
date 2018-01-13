package no.obos.util.servicebuilder.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public final class Version {
    public final int major;
    public final int minor;
    public final int patch;

    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }
}
