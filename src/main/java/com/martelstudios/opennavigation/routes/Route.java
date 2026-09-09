package com.martelstudios.opennavigation.routes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * One place a player can stand, and its link to the place they came from. A history is nothing but
 * routes holding each other: there is no list anywhere, so a route handed around alone still knows
 * its whole past and can be walked back through.
 * <p>
 * Every operation returns the tip the stack ends on, which is what the caller has to show next.
 */
public interface Route {

    /**
     * @return who this route belongs to, one mod's own word. Two mods are free to both call a route
     * "journal" without those being the same place, and it is the key a mod subscribes on to hear
     * about its own routes and no one else's.
     */
    @Nonnull
    String getNamespace();

    /**
     * @return the name identifying this route inside its namespace. Two routes of the same kind
     * sharing a namespace and a name are the same place, which is what going back to one is written
     * against — so a route standing for one thing among many carries which one in its name.
     */
    @Nonnull
    String getName();

    @Nullable
    Route getPrevious();

    /**
     * Seats this route behind another. Recomputes the depth of everything it carries, so a branch
     * moved elsewhere reports where it now sits rather than where it was built.
     */
    void setPrevious(@Nullable Route previous);

    @Nullable
    Route getNext();

    void setNext(@Nullable Route next);

    /**
     * @return whether this route is showing on its own account, ignoring what it hangs from.
     */
    boolean isSelfActive();

    void setSelfActive(boolean selfActive);

    /**
     * @return whether this route is showing at all. One hidden ancestor hides everything behind it.
     */
    boolean isActive();

    /**
     * @return how far from the root this route sits, the root being zero.
     */
    int getDepth();

    /**
     * Puts the given route on top of this one, dropping whatever this route already carried.
     */
    @Nonnull
    Route push(@Nonnull Route route);

    /**
     * Takes this route off the stack. A root has nothing to fall back to and stays.
     */
    @Nonnull
    Route pop();

    /**
     * Takes the given route off the stack, wherever it sits behind this one.
     */
    @Nonnull
    Route pop(@Nonnull Route route);

    /**
     * Goes back to the given route, cutting everything after it. Pushes it on the tip instead when
     * it is nowhere in the history, so navigating to somewhere new still gets there.
     */
    @Nonnull
    Route navigate(@Nonnull Route route);

    @Nonnull
    Route getRoot();

    @Nonnull
    Route getTip();

    /**
     * @return the matching route already in the history, or {@code null} if this is the first time.
     * Searched backwards only: what is ahead is where the player already is.
     */
    @Nullable
    Route search(@Nonnull Route route);

    boolean hasPrevious();

    boolean hasNext();
}
