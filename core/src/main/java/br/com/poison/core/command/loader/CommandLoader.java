package br.com.poison.core.command.loader;

import br.com.poison.core.command.inheritor.CommandInheritor;

public interface CommandLoader {

    void initClass(CommandInheritor command);

    void register(String path);
}
