# OpenNavigation

User interface navigation for Hytale. A player stands on a **route**; routes remember what they came
from, so going back is reading that history rather than rebuilding it.

A library for mod authors. It draws nothing and ships no assets — on its own a player sees no
difference. What it removes is the bookkeeping behind a back button and a breadcrumb trail.

## Why

A server holds **one custom page at a time**: `PageManager.openCustomPage` dismisses whatever stood
there. Two screens that need a way back, or two mods that both want a screen, and somebody has to
remember where the player was. That is the whole job here.

## The model

Ported from [UEX Navigation](https://github.com/UExtension), where a route is a node in a linked
history — it knows its previous, its next and its depth — and one service moves the player between
them. Nothing in that shape was Unity's.

A route is identified by its **namespace and its name**. The namespace is the mod that owns it, which
is both what keeps two mods' routes apart and the key they subscribe on. The name says which place
inside that mod, so a route standing for one thing among many carries which one in it:
`mymod` + `shop:item:1a2b`.

| Type | What it is |
| --- | --- |
| `StackRoute` | One place, carrying at most one other. |
| `TabRoute` | Several at once, one showing, each keeping a history of its own. |

Every route carries an optional `Object` context — the quest, the shop, whatever that place is
about — read back with `getContext(Class<T>)`. It is deliberately no part of the route's identity:
a route naming one thing among many says which in its **name**, or two of them are the same place
and going back to one lands on the other.

## Routers

A player holds one history per **router**, and every move names the router it applies to. A router
is identified like a route, by a namespace and a name: `new RouterId("mymod", "shop")`. So a mod
rooting its own interface never wipes where the player was in another one, and one interface can
keep several navigations side by side (two panes of a split screen, say), each with its own way back.

## Using it

```java
RouterId shop = new RouterId("mymod", "shop");

NavigationService.get().root(player, shop, new StackRoute("mymod", "shop"));
NavigationService.get().push(player, shop, new StackRoute("mymod", "shop:item:" + id));
NavigationService.get().pop(player, shop);
```

None of that opens a page. Each move writes the new tip down and dispatches a `RouteChangedEvent`,
keyed on the route's namespace and naming the router that moved:

```java
plugin.getEventRegistry().register(RouteChangedEvent.class, "mymod", this::draw);
```

So a mod hears about its own routes and no one else's, a route nobody claims reaches
`registerUnhandled` rather than silently doing nothing, and popping out of your screen into another
mod's tells *that* mod to draw, which is the only way a shared history could work, since no mod can
draw another's page. A listener drawing several routers reads `getRouter()` to know which to redraw.

`root` is the one move that needs no history behind it; everything else answers `null` on a router
never rooted. Which is the convention: an interface roots itself when it opens, and the rest are
moves. An interface reopened by a command that should come back where the player left it calls
`resume` instead, which roots only the first time:

```java
NavigationService.get().resume(player, shop, () -> new StackRoute("mymod", "shop"));
```

### One instance per player

Routes are mutable nodes and `setPrevious` rewrites the graph. Build a fresh route for each push —
a `static final Route` handed to two players would have the second tear the first's history apart.

### Nothing reaches the disk

`NavigationComponent` is registered without a codec, so a player's routers live as long as the
session. Where a player stands is a fact about right now, and handing it back after a restart would
drop them behind a screen they never opened.

## Building

```
./gradlew build
```

The jar lands in `build/libs/`. `./gradlew runServer` starts a dev server with the mod staged into
it — one plugin, so there is no workspace to run several of them.

`./gradlew test` runs the route model's tests, and `build` runs them too, so a broken history is a
broken build. The model is plain Java with no server behind it, which is what makes it worth
testing: push, pop and navigate are the whole product.

## Depending on it from another mod

```json
"Dependencies": {
  "MartelStudios:OpenNavigation": "*"
}
```

On the Gradle side, `requiredDependency` rather than `compileOnly`: `runAllMods` builds its classpath
from `runtimeClasspath`, and the workspace mods run on the system loader, which cannot reach into the
plugin loader that owns a jar in `run/mods`. The manifest inside the jar is found on that classpath,
so the plugin is discovered there too — staging it into `run/mods` as well is what a duplicate
plugin looks like.

## Releasing

The **Release** workflow, run from the Actions tab, writes the version into `gradle.properties`, tags
it and publishes the jar, then hands that same file to CurseForge — the one the release carries,
never one built again from the same sources.

It needs the `RELEASE_APP_ID`, `RELEASE_APP_PRIVATE_KEY` and `CURSEFORGE_TOKEN` secrets, and the
`CURSEFORGE_GAME_VERSION` variable holding the Hytale version id this release targets. The job fails
early and says so when either is missing, rather than uploading against the wrong version.

The CurseForge description lives in [docs/curseforge/opennavigation.md](docs/curseforge/opennavigation.md).

## License

MIT.
