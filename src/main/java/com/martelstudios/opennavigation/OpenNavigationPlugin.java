package com.martelstudios.opennavigation;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.martelstudios.opennavigation.components.NavigationComponent;

import javax.annotation.Nonnull;

/**
 * Navigation as a service other plugins call rather than a screen of its own: a player stands on a
 * route, routes remember what they came from, and going back is reading that history rather than
 * rebuilding it.
 */
public class OpenNavigationPlugin extends JavaPlugin {

    private static OpenNavigationPlugin instance;

    private ComponentType<EntityStore, NavigationComponent> navigationComponentType;

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

        // No codec, so nothing of it reaches the disk: where a player stands is a fact about this
        // session and about nothing else.
        navigationComponentType = getEntityStoreRegistry().registerComponent(NavigationComponent.class, NavigationComponent::new);
    }

    public ComponentType<EntityStore, NavigationComponent> getNavigationComponentType() {
        return navigationComponentType;
    }
}
