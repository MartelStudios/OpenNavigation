package com.martelstudios.opennavigation.routes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabRouteTest {

    private static final String NS = "test";

    private final Route active = new StackRoute(NS, "active");
    private final Route finished = new StackRoute(NS, "finished");
    private final TabRoute journal = new TabRoute(NS, "journal", List.of(active, finished), active);

    @Nested
    @DisplayName("the open tab")
    class OpenTab {

        @Test
        void is_the_one_the_group_was_built_on() {
            assertSame(active, journal.getActiveTab());
        }

        @Test
        @DisplayName("is what the group hands out as its next")
        void stands_in_for_next() {
            assertSame(active, journal.getNext());
            assertEquals(active, journal.getTip());
        }

        @Test
        @DisplayName("is the only one showing")
        void hides_the_others() {
            assertTrue(active.isActive());
            assertFalse(finished.isActive());
        }

        @Test
        void changes_which_one_shows() {
            journal.setActiveTab(new StackRoute(NS, "finished"));

            assertSame(finished, journal.getActiveTab());
            assertTrue(finished.isActive());
            assertFalse(active.isActive());
        }

        @Test
        @DisplayName("is named, not handed in: the caller need not hold the instance")
        void resolves_a_tab_by_name() {
            assertSame(finished, journal.setActiveTab(new StackRoute(NS, "finished")));
        }

        @Test
        void refuses_a_tab_the_group_does_not_have() {
            assertThrows(IllegalArgumentException.class, () -> journal.setActiveTab(new StackRoute(NS, "nowhere")));
            assertFalse(journal.trySetActiveTab(new StackRoute(NS, "nowhere")));
            assertTrue(journal.trySetActiveTab(new StackRoute(NS, "finished")));
        }
    }

    @Nested
    @DisplayName("in a history")
    class InAHistory {

        private final Route home = new StackRoute(NS, "home");

        @Test
        void numbers_its_tabs_below_itself() {
            home.push(journal);

            assertEquals(1, journal.getDepth());
            assertEquals(2, active.getDepth());
            assertEquals(2, finished.getDepth());
        }

        @Test
        @DisplayName("puts what is pushed inside the open tab")
        void pushes_into_the_open_tab() {
            home.push(journal);

            Route detail = new StackRoute(NS, "detail");
            assertEquals(detail, journal.push(detail));
            assertSame(active, detail.getPrevious());
            assertEquals(detail, home.getTip());
        }

        @Test
        @DisplayName("goes with the open tab when that tab is popped")
        void pops_whole() {
            home.push(journal);

            assertEquals(home, journal.getTip().pop(journal.getActiveTab()));
            assertFalse(home.hasNext());
        }

        @Test
        @DisplayName("opens the tab the caller asked for when navigated back to")
        void navigating_back_reopens_a_tab() {
            home.push(journal);
            Route detail = new StackRoute(NS, "detail");
            Route tip = journal.push(detail);

            TabRoute asked = new TabRoute(NS, "journal", List.of(new StackRoute(NS, "active"), new StackRoute(NS, "finished")), new StackRoute(NS, "finished"));

            assertEquals(finished, tip.navigate(asked));
            assertSame(finished, journal.getActiveTab());
        }
    }

    @Test
    @DisplayName("refuses to be given a next: opening a tab is the only way through it")
    void refuses_set_next() {
        assertThrows(UnsupportedOperationException.class, () -> journal.setNext(new StackRoute(NS, "detail")));
    }

    @Test
    void lists_its_tabs_in_declaration_order() {
        assertEquals(List.of(active, finished), journal.getTabs());
    }
}
