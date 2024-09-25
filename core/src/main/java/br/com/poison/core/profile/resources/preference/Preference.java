package br.com.poison.core.profile.resources.preference;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Preference {

    // Default Preference
    private boolean allowClanInteractions = true;

    private boolean allowDirectMessages = true;

    // Vip Preference
    private boolean joinMessage = true;

    private boolean flyingLobby = false;

    // Staff Preference
    private boolean allowStaffChatMessages = true;
    private boolean inStaffChat = false;

    private boolean allowLogs = true;
    private boolean autoVanishMode = false;

    private boolean buildMode = false;
}
