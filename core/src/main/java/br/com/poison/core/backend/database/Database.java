package br.com.poison.core.backend.database;

public interface Database {

    void init();

    void end();

    boolean hasConnection();
}
