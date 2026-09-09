package com.martelstudios.opennavigation.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.martelstudios.opennavigation.OpenNavigationPlugin;
import com.martelstudios.opennavigation.routes.Route;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Where one player stands. Registered without a codec, so it lives as long as the session and no
 * longer: a history is what the player did with the interface just now, and handing it back after a
 * restart would drop them behind a screen they never opened.
 */
public class NavigationComponent implements Component<EntityStore> {

    /**
     * The tip, not the root: everything else is reachable from it, and it is what has to be drawn.
     */
    @Nullable
    private Route route;

    public NavigationComponent() {}

    private NavigationComponent(@Nonnull NavigationComponent other) {
        this.route = other.route;
    }

    @Override
    public Component<EntityStore> clone() {
        return new NavigationComponent(this);
    }

    public static ComponentType<EntityStore, NavigationComponent> getComponentType() {
        return OpenNavigationPlugin.get().getNavigationComponentType();
    }

    /**
     * @return where the player stands, or {@code null} before they have gone anywhere.
     */
    @Nullable
    public Route getRoute() {
        return route;
    }

    /**
     * Records where the player now stands. Kept as the tip whatever is handed in, so a caller
     * holding a route from the middle of the history cannot leave the player standing behind
     * themselves.
     */
    public void setRoute(@Nullable Route route) {
        this.route = route == null ? null : route.getTip();
    }

    /**
     * @return {@code true} once the player stands somewhere, which is what tells a first visit from
     * a return.
     */
    public boolean hasRoute() {
        return route != null;
    }
}
