package br.com.poison.core.profile.resources.rank.tag.info;

import br.com.poison.core.profile.resources.rank.tag.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TagInfo {

    private Tag tag = Tag.PLAYER, lastTag = Tag.PLAYER;

    private long updatedAt = System.currentTimeMillis();

    public void update(Tag tag) {
        this.lastTag = getTag();
        this.tag = tag;

        this.updatedAt = System.currentTimeMillis();
    }
}
