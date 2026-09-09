package com.martelstudios.opennavigation;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

/**
 * Navigation as a service other plugins call rather than a screen of its own: a player stands on a
 * route, routes remember what they came from, and going back is reading that history rather than
 * rebuilding it.
 */
public class OpenNavigationPlugin extends JavaPlugin {

    private static OpenNavigationPlugin instance;

    public OpenNavigationPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static OpenNavigationPlugin get() {
        return instance;
    }

    @Override
    protected void setup() {
        super.setup();
    }
}
