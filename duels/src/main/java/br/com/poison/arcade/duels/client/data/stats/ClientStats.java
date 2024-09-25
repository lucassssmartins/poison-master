package br.com.poison.arcade.duels.client.data.stats;

import br.com.poison.arcade.duels.client.data.stats.type.ClientStatsType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientStats {

    private ClientStatsType type;
    private int duration;

    public ClientStats(ClientStatsType type) {
        this.type = type;
        this.duration = type.getDuration();
    }

    public boolean hasExpired() {
        return type.getDuration() != -1 && duration <= 0;
    }
}
