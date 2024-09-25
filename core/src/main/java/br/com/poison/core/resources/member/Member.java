package br.com.poison.core.resources.member;

import br.com.poison.core.backend.database.redis.message.type.LeagueUpdateMessage;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.league.League;
import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class Member {

    private final UUID id;
    private final String name;

    private League league;

    private int coins, xp;

    private final long createdAt;

    public Member(Profile profile) {
        this.id = profile.getId();
        this.name = profile.getName();

        this.league = League.INITIAL;

        this.createdAt = System.currentTimeMillis();
    }

    protected abstract void save(String... fields);

    /* League Methods */
    public void setLeague(League league) {
        this.league = league;
        save("league");
    }

    public void checkRank() {
        if (league.hasNext()) {
            League next = league.next();

            if (next.getMinimumXp() <= xp) {
                setLeague(next);

                new LeagueUpdateMessage(this, next).publish();
            }
        }
    }

    /* Economy Methods */
    public void setCoins(int coins) {
        this.coins = coins;
        save("coins");
    }

    public void addCoins(int coins) {
        if (coins > 0)
            setCoins(getCoins() + coins);
    }

    public void removeCoins(int coins) {
        if (coins > 0 && getCoins() >= coins)
            setCoins(getCoins() - coins);
    }

    public void setXp(int xp) {
        this.xp = xp;
        save("xp");
    }

    public void addXp(int xp) {
        if (xp > 0) {
            setXp(getXp() + xp);

            checkRank();
        }
    }

    public void removeXp(int xp) {
        if (xp > 0 && getXp() >= xp)
            setXp(getXp() - xp);
    }
}
