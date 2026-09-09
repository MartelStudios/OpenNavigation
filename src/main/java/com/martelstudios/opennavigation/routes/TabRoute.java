package com.martelstudios.opennavigation.routes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A route carrying several at once, only one of them showing. The tabs are not a history: switching
 * between them is not going anywhere, so none of them is ever pushed or popped — which is why this
 * route hands its active tab out as what comes next rather than holding a next of its own.
 */
public class TabRoute extends AbstractRoute {

    /** Kept in declaration order, since that is the order they are drawn in. */
    private final Map<String, Route> tabs = new LinkedHashMap<>();

    private Route activeTab;

    public TabRoute(@Nonnull String name, @Nonnull Collection<Route> tabs, @Nonnull Route activeTab) {
        super(name);

        for (Route tab : tabs) {
            this.tabs.put(tab.getName(), tab);
            tab.setPrevious(this);
            tab.setSelfActive(false);
        }

        setActiveTab(activeTab);
    }

    /**
     * The tabs hang from this route, so seating it elsewhere seats them too.
     */
    @Override
    public void setPrevious(@Nullable Route previous) {
        super.setPrevious(previous);

        for (Route tab : tabs.values()) {
            tab.setPrevious(this);
        }
    }

    /**
     * @return the tab showing. What comes after this route is whichever tab is open, so everything
     * written against {@code next} reaches the right branch without knowing about tabs.
     */
    @Nullable
    @Override
    public Route getNext() {
        return activeTab;
    }

    /**
     * @throws UnsupportedOperationException always. A tab route's next is the tab that is open, and
     * opening one is {@link #setActiveTab}.
     */
    @Override
    public void setNext(@Nullable Route next) {
        throw new UnsupportedOperationException("A tab route carries its tabs; open one with setActiveTab");
    }

    /**
     * Pushed onto the open tab rather than onto the group: what the player is looking at is the tab,
     * and coming back has to land there and not beside it.
     */
    @Nonnull
    @Override
    public Route push(@Nonnull Route route) {
        return getNext().push(route);
    }

    @Nonnull
    @Override
    public Route pop() {
        Route previous = getPrevious();

        return previous == null ? getTip() : previous.pop(this);
    }

    /**
     * A tab cannot be dropped on its own — the group would be left showing nothing — so asking to
     * pop the open one pops the whole group.
     */
    @Nonnull
    @Override
    public Route pop(@Nonnull Route route) {
        if (hasNext() && route.equals(getNext())) return pop();

        Route previous = getPrevious();

        return previous == null ? getTip() : previous.pop(route);
    }

    /**
     * Coming back to a tab group opens the tab the given one was asking for, rather than leaving
     * whichever was last open: navigating names a place, and here the tab is part of the place.
     */
    @Nonnull
    @Override
    public Route navigate(@Nonnull Route route) {
        if (equals(route)) {
            setActiveTab(((TabRoute) route).getActiveTab());
            return getTip();
        }

        Route previous = getPrevious();

        return previous == null ? push(route) : previous.navigate(route);
    }

    @Nonnull
    @Override
    public Route getTip() {
        return getNext().getTip();
    }

    @Nonnull
    public Route getActiveTab() {
        return activeTab;
    }

    /**
     * Opens a tab, by name: the caller names a place, not the instance this group was built with.
     *
     * @throws IllegalArgumentException if this group has no such tab.
     */
    @Nonnull
    public Route setActiveTab(@Nonnull Route tab) {
        Route opened = tabs.get(tab.getName());
        if (opened == null) {
            throw new IllegalArgumentException(getName() + " has no tab named " + tab.getName());
        }

        if (activeTab != null) activeTab.setSelfActive(false);

        activeTab = opened;
        activeTab.setSelfActive(true);

        return getTip();
    }

    /**
     * @return {@code false} if this group has no such tab, where {@link #setActiveTab} would throw.
     */
    public boolean trySetActiveTab(@Nonnull Route tab) {
        if (!tabs.containsKey(tab.getName())) return false;

        setActiveTab(tab);
        return true;
    }

    /**
     * @return the tabs of this group, in the order they were declared.
     */
    @Nonnull
    public List<Route> getTabs() {
        return List.copyOf(tabs.values());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Route tab : tabs.values()) {
            builder.append('\n')
                   .append('[').append(getName()).append(']')
                   .append(tab == activeTab ? '<' : '(')
                   .append(tab.getName())
                   .append(tab == activeTab ? '>' : ')')
                   .append(" => ").append(tab);
        }

        return builder.toString();
    }
}
