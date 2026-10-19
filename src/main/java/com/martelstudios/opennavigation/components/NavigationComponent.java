package com.martelstudios.opennavigation.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.martelstudios.opennavigation.OpenNavigationPlugin;
import com.martelstudios.opennavigation.routers.Router;
import com.martelstudios.opennavigation.routers.RouterId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Every navigation one player holds. Registered without a codec, so it lives as long as the session
 * and no longer: a history is what the player did with an interface just now, and handing it back
 * after a restart would drop them behind a screen they never opened.
 */
public class NavigationComponent implements Component<EntityStore> {

    private final Map<RouterId, Router> routers = new HashMap<>();

    public NavigationComponent() {}

    private NavigationComponent(@Nonnull NavigationComponent other) {
        this.routers.putAll(other.routers);
    }

    @Override
    public Component<EntityStore> clone() {
        return new NavigationComponent(this);
    }

    public static ComponentType<EntityStore, NavigationComponent> getComponentType() {
        return OpenNavigationPlugin.get().getNavigationComponentType();
    }

    /**
     * @return the router of that id, created empty the first time it is asked for.
     */
    @Nonnull
    public Router getRouter(@Nonnull RouterId id) {
        return routers.computeIfAbsent(id, Router::new);
    }

    /**
     * @return the router of that id, or {@code null} when nothing ever moved it. What a read asks,
     * so looking does not leave an empty router behind.
     */
    @Nullable
    public Router findRouter(@Nonnull RouterId id) {
        return routers.get(id);
    }
}
