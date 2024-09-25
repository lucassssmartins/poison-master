package br.com.poison.core.profile.exception.category;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProfileExceptionCategory {

    INVALID_SEARCH("Tipo de busca inválida, tente usar: %s");

    private final String message;
}