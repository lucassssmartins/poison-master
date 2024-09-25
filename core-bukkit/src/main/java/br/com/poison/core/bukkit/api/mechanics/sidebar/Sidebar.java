package br.com.poison.core.bukkit.api.mechanics.sidebar;

import br.com.poison.core.Constant;
import br.com.poison.core.util.Util;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.sidebar.row.Row;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Sidebar {

    protected transient final BukkitCore core = BukkitCore.getPlugin(BukkitCore.class);

    protected transient final String ROW_KEY = "sb_row:";

    private final Player owner;

    private final Scoreboard scoreboard;
    private final Objective objective;

    private String title;

    private final List<Row> rows;

    private int rowIndex, rowCount;
    @Setter
    private boolean showing;

    public Sidebar(Player owner, String title) {
        this.owner = owner;

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = scoreboard.getObjective(core.getName().toLowerCase());

        if (objective == null)
            objective = scoreboard.registerNewObjective(core.getName().toLowerCase(), "dummy");

        this.objective = objective;

        this.title = title;

        this.rows = new ArrayList<>();

        this.rowIndex = 0;
        this.rowCount = 0;

        // Objective Settings
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(Util.color(title.length() > 32 ? title.substring(0, 32) : title));

        owner.setScoreboard(scoreboard);
    }

    public void display() {
        for (Row row : rows) {
            if (row == null || row.getScore() == null) continue;

            Score score = row.getScore();

            score.setScore(rows.size() - rowIndex);

            rowIndex += 1;
        }

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.showing = true;

        BukkitCore.getSidebarManager().save(this);
    }

    public void clear() {
        scoreboard.getTeams().forEach(Team::unregister);
        scoreboard.getEntries().forEach(scoreboard::resetScores);

        rows.clear();

        rowIndex = 0;
        rowCount = 0;
    }

    public void setTitle(String title) {
        this.title = title;

        objective.setDisplayName(Util.color(title.length() > 32 ? title.substring(0, 32) : title));
    }

    public boolean hasRow(String tag) {
        return rows.stream().anyMatch(row -> row.getTag().equalsIgnoreCase(tag));
    }

    public Row getRow(String tag) {
        return rows.stream().filter(row -> row.getTag().equalsIgnoreCase(tag)).findFirst().orElse(null);
    }

    public void addRow(String tag, String prefix, String suffix) {
        if (!hasRow(tag)) {
            if (prefix.length() > 16)
                prefix = prefix.substring(0, 16);

            if (suffix.length() > 16)
                suffix = suffix.substring(0, 16);

            Row row = new Row(tag, prefix, suffix);

            Team team = scoreboard.registerNewTeam(ROW_KEY + tag.toLowerCase());

            team.setPrefix(Util.color(prefix));
            team.setSuffix(Util.color(suffix));

            team.addEntry(randomColor(rowCount));

            row.setTeam(team);

            Score score = objective.getScore(randomColor(rowCount));

            row.setScore(score);

            rows.add(row);
            rowCount++;
        }
    }

    public void addRow(String tag, String text) {
        if (text.length() > 16) {
            String prefix = text.substring(0, 16);
            String suffix = (Util.hasColor(text) ? Util.extractColor(text) : "") + text.substring(prefix.length());

            addRow(tag, prefix, suffix);
        } else
            addRow(tag, text, "");
    }

    public void updateRow(String tag, String prefix, String suffix) {
        if (hasRow(tag)) {
            Row row = getRow(tag);

            if (row == null || row.getTeam() == null) return;

            if (prefix.length() > 16)
                prefix = prefix.substring(0, 16);

            if (suffix.length() > 16)
                suffix = suffix.substring(0, 16);

            Team team = row.getTeam();

            team.setPrefix(prefix);
            team.setSuffix(suffix);
        }
    }

    public void updateRow(String tag, String suffix) {
        if (hasRow(tag)) {
            Row row = getRow(tag);

            if (row == null || row.getTeam() == null) return;

            if (suffix.length() > 16)
                suffix = suffix.substring(0, 16);

            Team team = row.getTeam();

            team.setSuffix(suffix);
        }
    }

    public void blankRow() {
        addRow(randomColor(rowCount), randomColor(rowCount));
    }

    public void addWebsiteRow(String color) {
        addRow("website", color + Constant.WEBSITE);
    }

    public void addWebsiteRow() {
        addWebsiteRow("&e");
    }

    protected String randomColor(int row) {
        return ChatColor.values()[row].toString() + ChatColor.RESET;
    }
}
