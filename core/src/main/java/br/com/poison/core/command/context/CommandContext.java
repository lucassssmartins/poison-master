package br.com.poison.core.command.context;

import br.com.poison.core.command.sender.CommandSender;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class CommandContext {

    private final CommandSender sender;

    private final String label;

    private final String[] args;

    public CommandContext(CommandSender sender, String label, String[] args, int subCommand) {
        String[] text = new String[args.length - subCommand];

        System.arraycopy(args, subCommand, text, 0, args.length - subCommand);

        StringBuilder builder = new StringBuilder();

        builder.append(label);

        for (int i = 0; i < subCommand; i++)
            builder.append(".").append(args[i]);

        String commandLabel = builder.toString();

        this.sender = sender;

        this.label = commandLabel;

        this.args = text;
    }

    public String getCommandLabel() {
        return label.replace(".", " ");
    }

    public String getMessage(int start, String... args) {
        StringBuilder message = new StringBuilder();

        for (int i = start; i < args.length; i++)
            message.append(args[i]).append(" ");

        return message.toString();
    }

    public Profile getProfile() {
        return Core.getProfileManager().read(sender.getUuid());
    }

    public Profile getProfile(UUID uuid) {
        return Core.getProfileData().read(uuid, false);
    }

    public Profile getProfile(String name) {
        return Core.getProfileData().read(name);
    }
}
