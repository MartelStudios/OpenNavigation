package com.martelstudios.opennavigation.services;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.martelstudios.opennavigation.OpenNavigationPlugin;
import com.martelstudios.opennavigation.components.NavigationComponent;
import com.martelstudios.opennavigation.events.RouteChangedEvent;
import com.martelstudios.opennavigation.routes.Route;
import com.martelstudios.opennavigation.routes.TabRoute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

/**
 * Moves a player through their history. Every move ends the same way: the tip is written down and a
 * {@link RouteChangedEvent} goes out. Nothing here opens a page, and nothing here knows what a route
 * looks like — that belongs to whoever declared the route.
 * <p>
 * Every move but {@link #root} needs the player to be somewhere already, and answers {@code null}
 * when they are not: an interface starts by rooting itself, and everything after that is a move.
 */
public class NavigationService {

    public static NavigationService get() {
        return OpenNavigationPlugin.get().getNavigationService();
    }

    /**
     * Starts a fresh history at the given route, dropping whatever the player had. What an interface
     * opening from nothing calls — a command, an interaction — since it is the one move that needs
     * no history behind it.
     */
    @Nullable
    public Route root(@Nonnull PlayerRef player, @Nonnull Route route) {
        Ref<EntityStore> reference = player.getReference();
        if (reference == null) return null;

        route.setPrevious(null);
        route.setSelfActive(true);

        return enter(player, reference, route);
    }

    /**
     * Goes one step further in, keeping where the player is underneath.
     */
    @Nullable
    public Route push(@Nonnull PlayerRef player, @Nonnull Route route) {
        return move(player, current -> current.push(route));
    }

    /**
     * Goes back one step. What a back arrow is bound to. A player standing on the root stays there.
     */
    @Nullable
    public Route pop(@Nonnull PlayerRef player) {
        return move(player, Route::pop);
    }

    /**
     * Goes back to a route already visited, cutting what followed; pushes it on the tip when the
     * history does not hold it.
     */
    @Nullable
    public Route navigate(@Nonnull PlayerRef player, @Nonnull Route route) {
        return move(player, current -> current.navigate(route));
    }

    /**
     * Swaps the tip for another route, leaving the history behind it untouched. A step the player
     * should not be able to come back to, such as a form that turned into its own result.
     */
    @Nullable
    public Route replace(@Nonnull PlayerRef player, @Nonnull Route route) {
        return move(player, current -> current.pop().push(route));
    }

    /**
     * Says again where the player stands, without moving them. Whoever draws that route redraws it,
     * which is how a listener refreshes an interface whose content changed underneath it.
     */
    @Nullable
    public Route reload(@Nonnull PlayerRef player) {
        return move(player, UnaryOperator.identity());
    }

    /**
     * Opens one tab of a group and lands on it, pushing the group first when the player is not
     * already inside it.
     */
    @Nullable
    public Route setActiveTab(@Nonnull PlayerRef player, @Nonnull TabRoute tabs, @Nonnull Route tab) {
        return move(player, current -> {
            Route known = current.search(tabs);

            if (known instanceof TabRoute opened) return opened.setActiveTab(tab);

            tabs.setActiveTab(tab);
            return current.push(tabs);
        });
    }

    /**
     * @return the matching route in this player's history, or {@code null} when they have never been
     * there. Reads only: nobody is moved and nothing is announced.
     */
    @Nullable
    public Route search(@Nonnull PlayerRef player, @Nonnull Route route) {
        Route current = getRoute(player);

        return current == null ? null : current.search(route);
    }

    /**
     * @return where the player stands, or {@code null} before they have gone anywhere this session.
     */
    @Nullable
    public Route getRoute(@Nonnull PlayerRef player) {
        Ref<EntityStore> reference = player.getReference();
        if (reference == null) return null;

        return component(reference).getRoute();
    }

    /**
     * Applies a move to where the player stands. Refuses to invent a starting point: a move with no
     * history behind it is a caller that forgot to {@link #root}.
     */
    @Nullable
    private Route move(@Nonnull PlayerRef player, @Nonnull UnaryOperator<Route> move) {
        Ref<EntityStore> reference = player.getReference();
        if (reference == null) return null;

        Route current = component(reference).getRoute();
        if (current == null) return null;

        return enter(player, reference, move.apply(current));
    }

    /**
     * Writes the move down, then tells whoever draws that route. In that order: a listener asking
     * where the player stands has to be answered the place it is being handed.
     */
    @Nonnull
    private Route enter(@Nonnull PlayerRef player, @Nonnull Ref<EntityStore> reference, @Nonnull Route route) {
        NavigationComponent component = component(reference);

        Route previous = component.getRoute();
        Route tip = route.getTip();

        component.setRoute(tip);

        HytaleServer.get()
                    .getEventBus()
                    .dispatchFor(RouteChangedEvent.class, tip.getNamespace())
                    .dispatch(new RouteChangedEvent(player, reference, tip, previous));

        return tip;
    }

    @Nonnull
    private static NavigationComponent component(@Nonnull Ref<EntityStore> reference) {
        return reference.getStore().ensureAndGetComponent(reference, NavigationComponent.getComponentType());
    }
}
