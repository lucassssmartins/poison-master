package br.com.poison.core.profile.exception;

import br.com.poison.core.profile.exception.category.ProfileExceptionCategory;

public class ProfileException extends Exception {

    public ProfileException(ProfileExceptionCategory category) {
        super(category.getMessage());
    }

    public ProfileException(ProfileExceptionCategory category, Object... replacers) {
        super(String.format(category.getMessage(), replacers));
    }
}
