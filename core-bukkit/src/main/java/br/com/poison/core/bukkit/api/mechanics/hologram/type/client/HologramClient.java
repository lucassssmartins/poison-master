package br.com.poison.core.bukkit.api.mechanics.hologram.type.client;

import br.com.poison.core.bukkit.api.mechanics.hologram.Hologram;
import br.com.poison.core.bukkit.api.mechanics.hologram.row.Row;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

@Getter
public class HologramClient extends Hologram {

    private final Player receiver;

    public HologramClient(Player receiver, String tag, Location location) {
        super(tag, location);

        this.receiver = receiver;
    }

    public HologramClient(Player receiver, String tag, Location location, long expiresAt) {
        super(tag, location, expiresAt);

        this.receiver = receiver;
    }

    @Override
    public synchronized void setText(List<String> lines) {
        if (getLines().isEmpty()) {
            Location loc = getLocation().clone();

            for (String line : lines) {
                getLines().add(new Row(this, loc, line));
                loc = loc.clone().subtract(0, 0.25, 0);
            }

            getLines().forEach(row -> row.spawn(receiver));
            return;
        }

        int diff = lines.size() - getLines().size();

        if (diff > 0) { // Adicionar novas linhas
            int index = 0;

            for (Row row : getLines()) {
                row.setText(lines.get(index++));
            }

            updateRows();

            int lastIndex = getLines().size() - 1;
            Row lastRow = getLines().get(lastIndex);
            Location loc = lastRow.getLocation().clone().subtract(0, 0.25, 0);

            while (diff > 0) {
                Row row = new Row(this, loc, lines.get(index++));

                getLines().add(row);
                row.spawn(receiver);

                loc = loc.clone().subtract(0, 0.25, 0);
                diff--;
            }
        } else { // Remove linhas e atualiza as restantes
            Iterator<Row> rowIterator = getLines().iterator();

            for (int i = 0; rowIterator.hasNext(); i++) {
                Row row = rowIterator.next();

                if (i < lines.size()) {
                    row.setText(lines.get(i));
                } else {
                    row.despawn(receiver);
                    rowIterator.remove();
                }
            }

            updateRows();
        }
    }

    @Override
    public void updateRows() {
        getLines().forEach(row -> row.update(receiver));
    }
}
