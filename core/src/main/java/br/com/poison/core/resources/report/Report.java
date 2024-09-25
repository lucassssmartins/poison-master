package br.com.poison.core.resources.report;

import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.report.info.ReportInfo;
import br.com.poison.core.util.extra.TimeUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Report {

    private final int id;

    private final UUID uniqueId;
    private final String name;

    private final Map<UUID, ReportInfo> infos;

    private UUID last;

    private long lastReportAt, expiresAt;

    public Report(Profile profile) {
        this.id = Core.getReportData().size() + 1;

        this.uniqueId = profile.getId();
        this.name = profile.getName();

        this.infos = new HashMap<>();

        this.last = Constant.CONSOLE_UUID;
        this.lastReportAt = System.currentTimeMillis();

        this.expiresAt = TimeUtil.getTime("30m");
    }

    /**
     * Salvar os campos do report.
     */
    protected void save() {
        Core.getMultiService().async(() -> Core.getReportData().update(this));
    }

    public Profile getProfile() {
        return Core.getProfileData().read(uniqueId, false);
    }

    /* Report System */
    public boolean hasExpired() {
        return expiresAt <= System.currentTimeMillis();
    }

    /* Info System */
    public List<ReportInfo> listInfos() {
        return new ArrayList<>(infos.values());
    }

    public void addInfo(UUID author, String reason) {
        if (infos.containsKey(author)) return;

        expiresAt = TimeUtil.getTime("30m");

        last = author;
        lastReportAt = System.currentTimeMillis();

        infos.put(author, new ReportInfo(author, reason));

        save();
    }

    public void removeInfo(UUID author) {
        infos.remove(author);
        save();
    }
}
