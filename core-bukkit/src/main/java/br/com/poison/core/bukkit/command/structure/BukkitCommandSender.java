package br.com.poison.core.bukkit.command.structure;

import br.com.poison.core.Constant;
import br.com.poison.core.command.sender.CommandSender;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class BukkitCommandSender implements CommandSender {

    private final org.bukkit.command.CommandSender sender;

    @Override
    public UUID getUuid() {
        return isPlayer() ? ((Player) sender).getUniqueId() : Constant.CONSOLE_UUID;
    }

    @Override
    public String getName() {
        return isPlayer() ? sender.getName() : "CONSOLE";
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    @Override
    public void sendMessage(String... message) {
        sender.sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent message) {
        if (!isPlayer()) {
            sender.sendMessage("§cO console não suporte este tipo de mensagem!");
            return;
        }

        Player player = (Player) sender;

        if (player != null)
            player.sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent... message) {
        if (!isPlayer()) {
            sender.sendMessage("§cO console não suporte este tipo de mensagem!");
            return;
        }

        Player player = (Player) sender;

        if (player != null)
            player.sendMessage(message);
    }
}
