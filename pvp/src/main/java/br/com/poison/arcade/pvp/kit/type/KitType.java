package br.com.poison.arcade.pvp.kit.type;

public enum KitType {
    PRIMARY, SECONDARY;

    public int getId() {
        return ordinal() + 1;
    }
}
