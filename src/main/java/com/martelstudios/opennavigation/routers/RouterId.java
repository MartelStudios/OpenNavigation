package com.martelstudios.opennavigation.routers;

import javax.annotation.Nonnull;

/**
 * Names one navigation of a player, the way a route names one place: the namespace is the mod that
 * owns it, the name says which of its navigations. Two mods both calling theirs "main" keep two.
 */
public record RouterId(@Nonnull String namespace, @Nonnull String name) {

    @Override
    public String toString() {
        return namespace + ":" + name;
    }
}
