package br.com.poison.arcade.duels;

import br.com.poison.arcade.duels.game.list.standard.simulator.kit.manager.KitManager;
import br.com.poison.arcade.duels.manager.GameManager;
import br.com.poison.arcade.duels.manager.ClientManager;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.slime.api.SlimeWorldAPI;
import br.com.poison.core.bukkit.command.structure.BukkitCommandLoader;
import br.com.poison.core.bukkit.listener.loader.ListenerLoader;
import br.com.poison.core.bukkit.service.server.options.ServerOptions;
import br.com.poison.core.server.category.ServerCategory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

public class Duels extends BukkitCore {

    @Getter
    protected static ClientManager clientManager;

    @Getter
    protected static GameManager gameManager;

    @Override
    public void onLoad() {
        super.onLoad();

        getLogger().info("Iniciando Duels...");

        getLogger().info("Salvando configuração padrão...");
        saveDefaultConfig();

        /* Configurando opções de eventos do servidor */
        ServerOptions.BLOCK_BREAK = true;
        ServerOptions.BLOCK_PLACE = true;

        ServerOptions.DROP_ITEM_ENABLED = true;
        ServerOptions.DAMAGE_ENABLED = true;

        ServerOptions.DEFAULT_CHAT = false;
        ServerOptions.DEFAULT_TAB_LIST = false;

        ServerOptions.FOOD_ENABLED = true;
        /* Finalizando configuração dos eventos */

        Core.setServerCategory(ServerCategory.DUELS);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // Slime World
        SlimeWorldAPI.registerHikari("u23_95MiS1i9h2", "slime_duels", "946t1=!NPUcb@ypB8tZaySvJ");

        /* Iniciando Gerenciadores */
        clientManager = new ClientManager();

        gameManager = new GameManager(this);
        gameManager.handle();

        KitManager.handle();

        /* Gerenciadores iniciados */

        // Registrando os comandos do servidor
        new BukkitCommandLoader(this).register("br.com.poison.arcade.duels.command");
        // Registrando os eventos do servidor
        new ListenerLoader(this).register("br.com.poison.arcade.duels");

        // Adicionando nova receita
        addRecipes();

        getLogger().info("Duels inicializado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        gameManager.unload();

        getLogger().info("Duels encerrado com sucesso.");
    }

    public void addRecipes() {
        ShapelessRecipe recipe = new ShapelessRecipe(new ItemStack(Material.MUSHROOM_SOUP))
                .addIngredient(Material.BOWL);

        Bukkit.addRecipe(recipe.addIngredient(Material.INK_SACK, 3));
        Bukkit.addRecipe(recipe.addIngredient(Material.CACTUS));
    }
}
