package br.com.poison.core.manager;

import br.com.poison.core.resources.invitation.Invitation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class InvitationManager {

    private final List<Invitation> invitations = new ArrayList<>();

    public void save(Invitation invitation) {
        invitations.add(invitation);
    }

    public Invitation read(UUID sender, UUID target) {
        return invitations.stream()
                .filter(invitation -> !invitation.hasExpired()
                        && invitation.getSender().equals(sender) && invitation.getTarget().equals(target))
                .findFirst()
                .orElse(null);
    }

    public Invitation read(Predicate<Invitation> filter) {
        return invitations.stream().filter(filter).findFirst().orElse(null);
    }

    public void remove(Invitation invitation) {
        invitations.remove(invitation);
    }

    public boolean hasPendentRequest(UUID sender, UUID target) {
        return read(sender, target) != null;
    }

    public boolean hasPendentRequest(Predicate<Invitation> filter) {
        return read(filter) != null;
    }

    public void checkInvitations() {
        if (invitations.isEmpty()) return;

        invitations.removeIf(Invitation::hasExpired);
    }
}
