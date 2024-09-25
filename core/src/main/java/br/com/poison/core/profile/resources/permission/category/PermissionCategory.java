package br.com.poison.core.profile.resources.permission.category;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionCategory {

    MEDAL("cosmetic.medal.%s"),
    TAG("rank.tag.%s"),
    COMMAND("server.command.%s");

    private final String key;
}
