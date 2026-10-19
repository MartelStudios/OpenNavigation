package com.martelstudios.opennavigation.routers;

import com.martelstudios.opennavigation.routes.Route;
import com.martelstudios.opennavigation.routes.StackRoute;
import com.martelstudios.opennavigation.routes.TabRoute;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouterTest {

    private static final String NS = "test";

    private final Router journal = new Router(new RouterId(NS, "journal"));
    private final Router shop = new Router(new RouterId(NS, "shop"));

    @Test
    @DisplayName("refuses every move before it is rooted")
    void needs_a_root() {
        assertNull(journal.push(new StackRoute(NS, "quest")));
        assertNull(journal.pop());
        assertNull(journal.navigate(new StackRoute(NS, "quest")));
        assertFalse(journal.hasRoute());
    }

    @Test
    void stands_on_the_tip_of_its_history() {
        journal.root(new StackRoute(NS, "list"));
        Route quest = journal.push(new StackRoute(NS, "quest"));

        assertEquals(new StackRoute(NS, "quest"), quest);
        assertSame(quest, journal.getRoute());
        assertEquals(new StackRoute(NS, "list"), journal.pop());
    }

    @Test
    @DisplayName("keeps its history apart from another router's")
    void routers_are_independent() {
        journal.root(new StackRoute(NS, "list"));
        journal.push(new StackRoute(NS, "quest"));

        shop.root(new StackRoute(NS, "shop"));

        assertEquals(new StackRoute(NS, "quest"), journal.getRoute());
        assertEquals(1, journal.getRoute().getDepth());
        assertEquals(new StackRoute(NS, "shop"), shop.getRoute());
    }

    @Test
    void rooting_again_drops_the_old_history() {
        journal.root(new StackRoute(NS, "list"));
        journal.push(new StackRoute(NS, "quest"));

        Route fresh = journal.root(new StackRoute(NS, "other"));

        assertFalse(fresh.hasPrevious());
        assertNull(journal.search(new StackRoute(NS, "list")));
    }

    @Test
    @DisplayName("pushes a tab group it does not hold, then switches tabs inside it")
    void opens_tabs() {
        journal.root(new StackRoute(NS, "home"));

        Route active = new StackRoute(NS, "active");
        Route finished = new StackRoute(NS, "finished");
        TabRoute tabs = new TabRoute(NS, "tabs", List.of(active, finished), active);

        assertSame(finished, journal.setActiveTab(tabs, new StackRoute(NS, "finished")));
        assertSame(active, journal.setActiveTab(new TabRoute(NS, "tabs", List.of(new StackRoute(NS, "active"), new StackRoute(NS, "finished")), new StackRoute(NS, "active")), new StackRoute(NS, "active")));
        assertTrue(active.isActive());
        assertEquals(2, journal.getRoute().getDepth());
    }
}
