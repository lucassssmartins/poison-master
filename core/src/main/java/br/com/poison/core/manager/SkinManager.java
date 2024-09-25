package br.com.poison.core.manager;

import br.com.poison.core.Core;
import br.com.poison.core.resources.skin.Skin;
import br.com.poison.core.resources.skin.category.SkinCategory;
import br.com.poison.core.resources.skin.texture.SkinTexture;
import com.mojang.authlib.properties.Property;

import java.util.UUID;
import java.util.logging.Level;

public class SkinManager {

    public Skin fetch(UUID uuid, String name, SkinCategory category) {
        Skin skin = null;

        try {
            Property textures = Core.MOJANG_API.getTextures(uuid);

            if (textures != null)
                skin = new Skin(name, category, new SkinTexture(textures.getValue(), textures.getSignature()));
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Skin \"" + uuid + "\" not found.", e);
        }

        return skin;
    }
}
