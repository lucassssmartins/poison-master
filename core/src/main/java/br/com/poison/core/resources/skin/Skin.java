package br.com.poison.core.resources.skin;

import br.com.poison.core.resources.skin.texture.SkinTexture;
import br.com.poison.core.resources.skin.category.SkinCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@AllArgsConstructor
@ToString
public class Skin {

    private final UUID id = UUID.randomUUID();
    private final String name;

    private final SkinCategory category;
    private final SkinTexture texture;

    @Override
    public boolean equals(Object skinObj) {
        if (this == skinObj) return true;
        if (skinObj == null || getClass() != skinObj.getClass()) return false;

        Skin skin = (Skin) skinObj;

        return skin.getId().equals(id) && skin.getTexture().equals(texture);
    }
}
