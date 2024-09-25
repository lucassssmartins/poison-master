package br.com.poison.core.util.mojang;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MojangException extends RuntimeException {

    private final ErrorType errorType;

    public enum ErrorType {

        INVALID_UUID,
        INVALID_NICKNAME,
        UNKNOWN,

    }


}