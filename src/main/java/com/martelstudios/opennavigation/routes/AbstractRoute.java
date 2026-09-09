package com.martelstudios.opennavigation.routes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * What every route shares: its name, where it sits, and how far it is from the root. What it means
 * to push onto it or come back to it is left to the kinds below, since a stack and a set of tabs
 * answer that differently.
 */
public abstract class AbstractRoute implements Route {

    private final String name;

    @Nullable
    private Route previous;

    @Nullable
    private Route next;

    private boolean selfActive = true;

    private int depth;

    protected AbstractRoute(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public Route getPrevious() {
        return previous;
    }

    @Override
    public void setPrevious(@Nullable Route previous) {
        this.previous = previous;

        // Kept rather than walked: the depth is read on every draw, the chain is relinked rarely
        depth = previous == null ? 0 : previous.getDepth() + 1;

        // Everything this route carries moved with it, and has its own depth to correct
        if (hasNext()) getNext().setPrevious(this);
    }

    @Nullable
    @Override
    public Route getNext() {
        return next;
    }

    @Override
    public void setNext(@Nullable Route next) {
        this.next = next;
    }

    @Override
    public boolean isSelfActive() {
        return selfActive;
    }

    @Override
    public void setSelfActive(boolean selfActive) {
        this.selfActive = selfActive;
    }

    @Override
    public boolean isActive() {
        return (previous == null || previous.isActive()) && selfActive;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Nonnull
    @Override
    public Route getRoot() {
        return previous == null ? this : previous.getRoot();
    }

    @Nullable
    @Override
    public Route search(@Nonnull Route route) {
        if (equals(route)) return this;

        return previous == null ? null : previous.search(route);
    }

    @Override
    public boolean hasPrevious() {
        return previous != null;
    }

    /**
     * Asked through {@link #getNext()} rather than the field, since a kind of route can carry
     * something it does not hold in one.
     */
    @Override
    public boolean hasNext() {
        return getNext() != null;
    }

    /**
     * A name identifies a place, so two routes of the same kind carrying the same name are the same
     * place — whatever else they were built with. Coming back to a route is written against this.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        return name.equals(((AbstractRoute) other).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
