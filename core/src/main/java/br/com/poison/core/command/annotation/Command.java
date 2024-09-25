package br.com.poison.core.command.annotation;

import br.com.poison.core.profile.resources.rank.category.RankCategory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    String name();

    String[] aliases() default {};

    RankCategory rank() default RankCategory.PLAYER;

    String permission() default "";

    String usage() default "";

    int expectedArguments() default -1;

    boolean onlyPlayer() default true;

    boolean runAsync() default false;
}
