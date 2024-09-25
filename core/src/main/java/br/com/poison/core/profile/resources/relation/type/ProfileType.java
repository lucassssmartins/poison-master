package br.com.poison.core.profile.resources.relation.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProfileType {

    PIRATE("Conta pirata"),
    AUTHENTIC("Conta autêntica");

    private final String info;
}
