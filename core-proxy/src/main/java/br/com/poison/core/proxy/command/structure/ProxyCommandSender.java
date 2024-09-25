package br.com.poison.core.proxy.command.structure;

import br.com.poison.core.Constant;
import br.com.poison.core.command.sender.CommandSender;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@RequiredArgsConstructor
public class ProxyCommandSender implements CommandSender {

    private final net.md_5.bungee.api.CommandSender sender;

    @Override
    public UUID getUuid() {
        return isPlayer() ? ((ProxiedPlayer) sender).getUniqueId() : Constant.CONSOLE_UUID;
    }

    @Override
    public String getName() {
        return isPlayer() ? sender.getName() : "CONSOLE";
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof ProxiedPlayer;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages)
            sender.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(BaseComponent message) {
        sender.sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent... message) {
        sender.sendMessage(message);
    }
}
