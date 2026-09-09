package com.martelstudios.opennavigation.routes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackRouteTest {

    private final Route home = new StackRoute("home");
    private final Route list = new StackRoute("list");
    private final Route detail = new StackRoute("detail");

    @Nested
    @DisplayName("push")
    class Push {

        @Test
        void returns_the_new_tip() {
            assertEquals(detail, home.push(list).push(detail));
        }

        @Test
        void links_both_ways() {
            home.push(list);

            assertSame(list, home.getNext());
            assertSame(home, list.getPrevious());
        }

        @Test
        void numbers_the_depth_from_the_root() {
            home.push(list).push(detail);

            assertEquals(0, home.getDepth());
            assertEquals(1, list.getDepth());
            assertEquals(2, detail.getDepth());
        }

        @Test
        @DisplayName("drops what the route already carried")
        void drops_the_branch_not_taken() {
            home.push(list).push(detail);

            Route other = new StackRoute("other");
            home.push(other);

            assertSame(other, home.getNext());
            assertNull(list.getPrevious());
        }

        @Test
        @DisplayName("carries a whole branch, renumbering it")
        void renumbers_a_moved_branch() {
            list.push(detail);
            home.push(list);

            assertEquals(2, detail.getDepth());
        }
    }

    @Nested
    @DisplayName("pop")
    class Pop {

        @Test
        void returns_the_route_below() {
            Route tip = home.push(list).push(detail);

            assertEquals(list, tip.pop());
            assertNull(list.getNext());
        }

        @Test
        @DisplayName("takes a named route off wherever it sits")
        void removes_a_route_from_the_middle() {
            Route tip = home.push(list).push(detail);

            assertEquals(home, tip.pop(list));
            assertNull(home.getNext());
        }

        @Test
        @DisplayName("leaves the root standing, there being nothing behind it")
        void keeps_the_root() {
            assertEquals(home, home.pop());
        }

        @Test
        void ignores_a_route_that_is_not_there() {
            Route tip = home.push(list);

            assertEquals(list, tip.pop(new StackRoute("nowhere")));
            assertSame(list, home.getNext());
        }
    }

    @Nested
    @DisplayName("navigate")
    class Navigate {

        @Test
        @DisplayName("goes back to a route already visited, cutting what followed")
        void returns_to_a_known_route() {
            Route tip = home.push(list).push(detail);

            assertEquals(list, tip.navigate(new StackRoute("list")));
            assertNull(list.getNext());
        }

        @Test
        @DisplayName("pushes a route nobody has visited onto the tip")
        void pushes_an_unknown_route() {
            Route tip = home.push(list);
            Route unknown = new StackRoute("unknown");

            assertEquals(unknown, tip.navigate(unknown));
            assertEquals(2, unknown.getDepth());
        }
    }

    @Nested
    class Reading {

        @Test
        void root_is_the_far_end() {
            Route tip = home.push(list).push(detail);

            assertEquals(home, tip.getRoot());
        }

        @Test
        void tip_is_the_near_end() {
            home.push(list).push(detail);

            assertEquals(detail, home.getTip());
        }

        @Test
        @DisplayName("search looks backwards only")
        void search_walks_back() {
            Route tip = home.push(list).push(detail);

            assertSame(home, tip.search(new StackRoute("home")));
            assertNull(home.search(new StackRoute("detail")));
        }

        @Test
        void a_route_is_showing_unless_told_otherwise() {
            home.push(list);

            assertTrue(list.isActive());
        }

        @Test
        @DisplayName("one hidden route hides everything it carries")
        void hiding_a_route_hides_its_tail() {
            home.push(list).push(detail);
            home.setSelfActive(false);

            assertTrue(detail.isSelfActive());
            assertEquals(false, detail.isActive());
        }
    }

    @Nested
    @DisplayName("identity")
    class Identity {

        @Test
        @DisplayName("is the name, so a route rebuilt names the same place")
        void equals_by_name() {
            assertEquals(new StackRoute("home"), home);
        }

        @Test
        @DisplayName("is not shared across kinds of route")
        void differs_by_kind() {
            TabRoute tabs = new TabRoute("home", java.util.List.of(new StackRoute("a")), new StackRoute("a"));

            org.junit.jupiter.api.Assertions.assertNotEquals(home, tabs);
        }
    }
}
