package br.com.poison.core.resources.punishment.reason;

import br.com.poison.core.resources.punishment.category.PunishmentCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum PunishmentReason {

    /* Ban Reasons */
    CHEATING("Uso de trapaças", PunishmentCategory.BAN, "-1L"),
    RACISM("Racismo", PunishmentCategory.BAN, "-1L"),

    /* Mute Reasons */
    SPAM("Excesso de mensagens", PunishmentCategory.MUTE, "15m"),
    INSULTS("Insultos", PunishmentCategory.MUTE, "1h"),
    DISCLOSURE("Divulgação", PunishmentCategory.MUTE, "1h");

    private final String info;
    private final PunishmentCategory category;

    private final String time;

    public static PunishmentReason fetch(String name) {
        return Arrays.stream(values())
                .filter(reason -> reason.name().equalsIgnoreCase(name) || reason.getInfo().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static PunishmentReason fetch(String name, PunishmentCategory category) {
        return Arrays.stream(values())
                .filter(reason -> (reason.name().equalsIgnoreCase(name) || reason.getInfo().equalsIgnoreCase(name)) && reason.getCategory().equals(category))
                .findFirst()
                .orElse(null);
    }

    public static List<PunishmentReason> list(PunishmentCategory category) {
        return Arrays.stream(values())
                .filter(reason -> reason.getCategory().equals(category))
                .collect(Collectors.toList());
    }
}
