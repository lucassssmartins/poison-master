package br.com.poison.core.resources.report.info;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReportInfo {

    private final UUID author;
    private final String reason;

    private final long createdAt = System.currentTimeMillis();
}
