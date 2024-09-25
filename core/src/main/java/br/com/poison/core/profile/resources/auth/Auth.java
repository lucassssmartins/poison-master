package br.com.poison.core.profile.resources.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Auth {

    private String password = "";

    private boolean registered = false;
    private long date = System.currentTimeMillis();

    public void update(String password) {
        this.password = password;

        this.registered = true;
        this.date = System.currentTimeMillis();
    }

    public boolean checkPassword(String password) {
        return this.password.equalsIgnoreCase(password);
    }
}
