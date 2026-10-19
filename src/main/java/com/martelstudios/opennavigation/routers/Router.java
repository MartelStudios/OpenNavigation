package com.martelstudios.opennavigation.routers;

import com.martelstudios.opennavigation.routes.Route;
import com.martelstudios.opennavigation.routes.TabRoute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

/**
 * One navigation: a history and the place it currently stands on. A player holds as many as the
 * interfaces they use, so one interface rooting itself never wipes where another one was.
 * <p>
 * Every move but {@link #root} needs a history already, and answers {@code null} without one: an
 * interface starts by rooting its router, and everything after that is a move.
 */
public class Router {

    @Nonnull
    private final RouterId id;

    /**
     * The tip, not the root: everything else is reachable from it, and it is what has to be drawn.
     */
    @Nullable
    private Route route;

    public Router(@Nonnull RouterId id) {
        this.id = id;
    }

    @Nonnull
    public RouterId getId() {
        return id;
    }

    /**
     * @return where this navigation stands, or {@code null} before it was ever rooted.
     */
    @Nullable
    public Route getRoute() {
        return route;
    }

    /**
     * @return {@code true} once this navigation was rooted. Nothing clears it: an interface opening
     * afresh roots a new history over the old one.
     */
    public boolean hasRoute() {
        return route != null;
    }

    /**
     * Starts a fresh history at the given route, dropping whatever this router held. The one move
     * that needs no history behind it.
     */
    @Nonnull
    public Route root(@Nonnull Route route) {
        route.setPrevious(null);
        route.setSelfActive(true);

        return stand(route);
    }

    /**
     * Goes one step further in, keeping the current place underneath.
     */
    @Nullable
    public Route push(@Nonnull Route route) {
        return move(current -> current.push(route));
    }

    /**
     * Goes back one step. A router standing on its root stays there.
     */
    @Nullable
    public Route pop() {
        return move(Route::pop);
    }

    /**
     * Goes back to a route already visited, cutting what followed; pushes it on the tip when the
     * history does not hold it.
     */
    @Nullable
    public Route navigate(@Nonnull Route route) {
        return move(current -> current.navigate(route));
    }

    /**
     * Swaps the tip for another route, leaving the history behind it untouched. A step that should
     * not be come back to, such as a form that turned into its own result.
     */
    @Nullable
    public Route replace(@Nonnull Route route) {
        return move(current -> current.pop().push(route));
    }

    /**
     * Opens one tab of a group and lands on it, pushing the group first when the history does not
     * hold it yet.
     */
    @Nullable
    public Route setActiveTab(@Nonnull TabRoute tabs, @Nonnull Route tab) {
        return move(current -> {
            if (current.search(tabs) instanceof TabRoute opened) return opened.setActiveTab(tab);

            tabs.setActiveTab(tab);
            return current.push(tabs);
        });
    }

    /**
     * @return the matching route in this history, or {@code null} when it was never there.
     */
    @Nullable
    public Route search(@Nonnull Route route) {
        return this.route == null ? null : this.route.search(route);
    }

    /**
     * Refuses to invent a starting point: a move with no history behind it is a caller that forgot
     * to {@link #root}.
     */
    @Nullable
    private Route move(@Nonnull UnaryOperator<Route> move) {
        return route == null ? null : stand(move.apply(route));
    }

    /**
     * Kept as the tip whatever the move handed back, so a route from the middle of the history
     * cannot leave this router standing behind itself.
     */
    @Nonnull
    private Route stand(@Nonnull Route route) {
        this.route = route.getTip();
        return this.route;
    }

    @Override
    public String toString() {
        return id + " @ " + route;
    }
}
