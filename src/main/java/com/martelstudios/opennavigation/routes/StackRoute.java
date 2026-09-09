package com.martelstudios.opennavigation.routes;

import javax.annotation.Nonnull;

/**
 * A route carrying at most one other: the ordinary way forward, where going somewhere new hides
 * what came before it and coming back reveals it again.
 */
public class StackRoute extends AbstractRoute {

    public StackRoute(@Nonnull String name) {
        super(name);
    }

    /**
     * Whatever this route already carried is cut loose rather than kept aside: a player who went
     * back and then took another way has no branch left to return to.
     */
    @Nonnull
    @Override
    public Route push(@Nonnull Route route) {
        if (hasNext()) getNext().setPrevious(null);

        setNext(route);
        route.setPrevious(this);

        return getTip();
    }

    @Nonnull
    @Override
    public Route pop() {
        Route previous = getPrevious();

        return previous == null ? getTip() : previous.pop(this);
    }

    @Nonnull
    @Override
    public Route pop(@Nonnull Route route) {
        if (hasNext() && route.equals(getNext())) {
            setNext(null);
            return getTip();
        }

        Route previous = getPrevious();

        return previous == null ? getTip() : previous.pop(route);
    }

    @Nonnull
    @Override
    public Route navigate(@Nonnull Route route) {
        if (equals(route)) {
            setNext(null);
            return getTip();
        }

        Route previous = getPrevious();

        return previous == null ? getTip().push(route) : previous.navigate(route);
    }

    @Nonnull
    @Override
    public Route getTip() {
        Route next = getNext();

        return next == null ? this : next.getTip();
    }

    @Override
    public String toString() {
        return getName() + " => " + (hasNext() ? getNext().toString() : "|||");
    }
}
