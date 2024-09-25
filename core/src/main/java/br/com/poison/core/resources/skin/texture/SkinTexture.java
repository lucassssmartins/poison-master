package br.com.poison.core.resources.skin.texture;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SkinTexture {
    private final String value, signature;

    @Override
    public boolean equals(Object textureObj) {
        if (this == textureObj) return true;
        if (textureObj == null || getClass() != textureObj.getClass()) return false;

        SkinTexture texture = (SkinTexture) textureObj;

        return texture.getValue().equalsIgnoreCase(value) && texture.getSignature().equalsIgnoreCase(signature);
    }
}