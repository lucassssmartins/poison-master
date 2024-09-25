package br.com.poison.core.resources.skin.category;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SkinCategory {

    PROFILE("Skin do perfíl"),
    CUSTOM("Skin customizada");

    private final String info;
}
