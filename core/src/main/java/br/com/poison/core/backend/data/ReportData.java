package br.com.poison.core.backend.data;

import br.com.poison.core.backend.database.redis.RedisDatabase;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.report.Report;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ReportData {

    private final RedisDatabase redis;

    private final String REPORT_KEY = "report:";

    public Report input(Profile profile) {
        Report report = new Report(profile);

        if (redis.exists(REPORT_KEY + report.getUniqueId())) return null;

        redis.save(REPORT_KEY + report.getUniqueId(), report);

        return report;
    }

    public void delete(UUID uniqueId) {
        redis.delete(REPORT_KEY + uniqueId);
    }

    public void update(Report report) {
        redis.update(REPORT_KEY + report.getUniqueId(), report);
    }

    public Report fetch(UUID uniqueId) {
        return redis.load(REPORT_KEY + uniqueId, Report.class);
    }

    public int size() {
        return redis.getTotalCount(REPORT_KEY);
    }

    public List<Report> listAll() {
        return redis.loadAll(REPORT_KEY, Report.class);
    }
}
