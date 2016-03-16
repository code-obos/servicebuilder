package no.obos.util.servicebuilder.exception.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProblemInformation {
    private int status;
    private String message;
}
