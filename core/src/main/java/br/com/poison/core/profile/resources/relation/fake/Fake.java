package br.com.poison.core.profile.resources.relation.fake;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Fake {

    private String nickname = "", lastNickname = "";

    private long appliedAt = System.currentTimeMillis();

    public void update(String fake) {
        this.lastNickname = (nickname == null || nickname.isEmpty()) ? fake : nickname;
        this.nickname = fake;

        this.appliedAt = System.currentTimeMillis();
    }
}
