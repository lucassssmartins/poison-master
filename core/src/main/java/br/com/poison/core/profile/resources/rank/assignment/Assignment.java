package br.com.poison.core.profile.resources.rank.assignment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Assignment {

    AUTO("Automaticamente atribuído"),
    STAFF("Atribuído por um membro da equipe"),
    CONSOLE("Atribuído pelo console");

    private final String message;
}
