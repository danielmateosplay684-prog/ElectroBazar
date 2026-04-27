package com.proconsi.electrobazar.model.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifactuSubmissionEvent {
    private final Long id;
    private final SubmissionType type;

    public enum SubmissionType {
        INVOICE,
        TICKET,
        RECTIFICATIVE,
        ANNULACION_INVOICE,
        ANNULACION_TICKET
    }
}
