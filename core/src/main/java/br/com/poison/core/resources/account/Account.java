package br.com.poison.core.resources.account;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Account {
    private final UUID uuid;
    private final String name;
}
