package com.martelstudios.opennavigation.services;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.martelstudios.opennavigation.OpenNavigationPlugin;
import com.martelstudios.opennavigation.components.NavigationComponent;
import com.martelstudios.opennavigation.events.RouteChangedEvent;
import com.martelstudios.opennavigation.routers.Router;
import com.martelstudios.opennavigation.routers.RouterId;
import com.martelstudios.opennavigation.routes.Route;
import com.martelstudios.opennavigation.routes.TabRoute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Moves a player through one of their navigations. Every move ends the same way: the router keeps
 * the new tip and a {@link RouteChangedEvent} goes out. Nothing here opens a page, and nothing here
 * knows what a route looks like: that belongs to whoever declared the route.
 * <p>
 * Every move names its router, so two interfaces never share a history unless they ask to. Every
 * move but {@link #root} and {@link #resume} answers {@code null} on a router never rooted.
 */
public class NavigationService {

    public static NavigationService get() {
        return OpenNavigationPlugin.get().getNavigationService();
    }

    /**
     * Starts a fresh history at the given route, dropping whatever that router held. What an
     * interface opening from nothing calls, since it is the one move that needs no history.
     */
    @Nullable
    public Route root(@Nonnull PlayerRef player, @Nonnull RouterId router, @Nonnull Route route) {
        return move(player, router, current -> current.root(route));
    }

    /**
     * Reopens a router where the player left it, or roots it on the given route the first time.
     * What an interface reopened by a command calls to come back where the player was.
     */
    @Nullable
    public Route resume(@Nonnull PlayerRef player, @Nonnull RouterId router, @Nonnull Supplier<Route> fresh) {
        return move(player, router, current -> current.hasRoute() ? current.getRoute() : current.root(fresh.get()));
    }

    /**
     * Goes one step further in, keeping where the player is underneath.
     */
    @Nullable
    public Route push(@Nonnull PlayerRef player, @Nonnull RouterId router, @Nonnull Route route) {
        return move(player, router, current -> current.push(route));
    }

    /**
     * Goes back one step. What a back arrow is bound to. A player standing on the root stays there.
     */
    @Nullable
    public Route pop(@Nonnull PlayerRef player, @Nonnull RouterId router) {
        return move(player, router, Router::pop);
    }

    /**
     * Goes back to a route already visited, cutting what followed; pushes it on the tip when the
     * history does not hold it.
     */
    @Nullable
    public Route navigate(@Nonnull PlayerRef player, @Nonnull RouterId router, @Nonnull Route route) {
        return move(player, router, current -> current.navigate(route));
    }

    /**
     * Swaps the tip for another route, leaving the history behind it untouched. A step the player
     * should not be able to come back to, such as a form that turned into its own result.
     */
    @Nullable
    public Route replace(@Nonnull PlayerRef player, @Nonnull RouterId router, @Nonnull Route route) {
        return move(player, router, current -> current.replace(route));
    }

    /**
     * Says again where the player stands, without moving them. Whoever draws that route redraws it,
     * which is how a listener refreshes an interface whose content changed underneath it.
     */
    @Nullable
    public Route reload(@Nonnull PlayerRef player, @Nonnull RouterId router) {
        return move(player, router, Router::getRoute);
    }

    /**
     * Opens one tab of a group and lands on it, pushing the group first when the player is not
     * already inside it.
     */
    @Nullable
    public Route setActiveTab(@Nonnull PlayerRef player, @Nonnull RouterId router, @Nonnull TabRoute tabs, @Nonnull Route tab) {
        return move(player, router, current -> current.setActiveTab(tabs, tab));
    }

    /**
     * @return the matching route in that history, or {@code null} when the player has never been
     * there. Reads only: nobody is moved and nothing is announced.
     */
    @Nullable
    public Route search(@Nonnull PlayerRef player, @Nonnull RouterId router, @Nonnull Route route) {
        Router found = find(player, router);

        return found == null ? null : found.search(route);
    }

    /**
     * @return where the player stands in that navigation, or {@code null} before it was rooted.
     */
    @Nullable
    public Route getRoute(@Nonnull PlayerRef player, @Nonnull RouterId router) {
        Router found = find(player, router);

        return found == null ? null : found.getRoute();
    }

    /**
     * Applies a move to one of the player's routers, then tells whoever draws where it landed. In
     * that order: a listener asking where the player stands has to be answered the place it is
     * being handed.
     */
    @Nullable
    private Route move(@Nonnull PlayerRef player, @Nonnull RouterId id, @Nonnull Function<Router, Route> move) {
        Ref<EntityStore> reference = player.getReference();
        if (reference == null) return null;

        Router router = component(reference).getRouter(id);

        Route previous = router.getRoute();
        Route tip = move.apply(router);
        if (tip == null) return null;

        HytaleServer.get()
                    .getEventBus()
                    .dispatchFor(RouteChangedEvent.class, tip.getNamespace())
                    .dispatch(new RouteChangedEvent(player, reference, id, tip, previous));

        return tip;
    }

    @Nullable
    private static Router find(@Nonnull PlayerRef player, @Nonnull RouterId router) {
        Ref<EntityStore> reference = player.getReference();

        return reference == null ? null : component(reference).findRouter(router);
    }

    @Nonnull
    private static NavigationComponent component(@Nonnull Ref<EntityStore> reference) {
        return reference.getStore().ensureAndGetComponent(reference, NavigationComponent.getComponentType());
    }
}
