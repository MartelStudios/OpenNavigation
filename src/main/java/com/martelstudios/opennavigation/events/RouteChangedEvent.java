package com.martelstudios.opennavigation.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.martelstudios.opennavigation.routes.Route;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fired every time a player ends up somewhere else, whichever way they got there. Keyed on the
 * namespace of the route they landed on, so a mod hears about its own routes and no one else's —
 * and a route nobody claims reaches {@code registerUnhandled} instead of silently doing nothing.
 * <p>
 * This is the whole of what this plugin asks of an interface. Drawing is the listener's: it opens
 * its own page, in its own frame, and this project never touches the player's page manager.
 */
public class RouteChangedEvent implements IEvent<String> {

    @Nonnull
    private final PlayerRef player;

    @Nonnull
    private final Ref<EntityStore> reference;

    @Nonnull
    private final Route route;

    @Nullable
    private final Route previous;

    public RouteChangedEvent(@Nonnull PlayerRef player, @Nonnull Ref<EntityStore> reference, @Nonnull Route route, @Nullable Route previous) {
        this.player = player;
        this.reference = reference;
        this.route = route;
        this.previous = previous;
    }

    @Nonnull
    public PlayerRef getPlayer() {
        return player;
    }

    /**
     * @return the player's entity, for a listener about to open a page. Live only for as long as
     * this dispatch, which runs on their world thread.
     */
    @Nonnull
    public Ref<EntityStore> getReference() {
        return reference;
    }

    /**
     * @return where the player now stands — always the tip, never a route in the middle.
     */
    @Nonnull
    public Route getRoute() {
        return route;
    }

    /**
     * @return where they stood before, or {@code null} on the first move of the session. A listener
     * that owns both can tell a move inside its own interface from an arrival into it.
     */
    @Nullable
    public Route getPrevious() {
        return previous;
    }
}
