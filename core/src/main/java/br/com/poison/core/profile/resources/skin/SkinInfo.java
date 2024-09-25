package br.com.poison.core.profile.resources.skin;

import br.com.poison.core.resources.skin.Skin;
import lombok.Getter;

@Getter
public class SkinInfo {

    private Skin skin, lastSkin;

    private long updatedAt = System.currentTimeMillis();

    public void update(Skin skinObj) {
        this.lastSkin = skin == null ? skinObj : skin;
        this.skin = skinObj;

        this.updatedAt = System.currentTimeMillis();
    }
}
