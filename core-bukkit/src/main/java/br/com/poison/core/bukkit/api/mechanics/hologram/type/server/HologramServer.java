package br.com.poison.core.bukkit.api.mechanics.hologram.type.server;

import br.com.poison.core.bukkit.api.mechanics.hologram.Hologram;
import br.com.poison.core.bukkit.api.mechanics.hologram.row.Row;
import org.bukkit.Location;

import java.util.Iterator;
import java.util.List;

public class HologramServer extends Hologram {

    public HologramServer(String tag, Location location) {
        super(tag, location);
    }

    public HologramServer(String tag, Location location, long expiresAt) {
        super(tag, location, expiresAt);
    }

    @Override
    public void teleport(Location location) {
        super.teleport(location);

        getLines().forEach(row -> getViewers().forEach(row::respawn));
    }

    @Override
    public synchronized void setText(List<String> lines) {
        if (getLines().isEmpty()) {
            Location loc = getLocation().clone();

            if (loc == null) return;

            for (String line : lines) {
                getLines().add(new Row(this, loc, line));
                loc = loc.clone().subtract(0, 0.25, 0);
            }

            getLines().forEach(row -> getViewers().forEach(row::spawn));
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
                getViewers().forEach(row::spawn);


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
                    getViewers().forEach(row::despawn);
                    rowIterator.remove();
                }
            }

            updateRows();
        }
    }

    @Override
    public void updateRows() {
        getLines().forEach(row -> getViewers().forEach(row::update));
    }
}
