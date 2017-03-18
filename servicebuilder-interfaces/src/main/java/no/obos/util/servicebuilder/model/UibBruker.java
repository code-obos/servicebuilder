package no.obos.util.servicebuilder.model;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/*
 * Representerer en bruker fra User Identity Backend
 */
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
public class UibBruker {
    public final String personid;
    public final String fornavn;
    public final String etternavn;
    public final String adBrukernavn;
    public final String userTokenId;
    public final ImmutableList<UibRolle> roller;
}
