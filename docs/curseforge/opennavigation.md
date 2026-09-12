# OpenNavigation

🧭 **User interface navigation for Hytale. Back buttons and breadcrumb trails, without writing the history yourself.**

A library for mod authors. On its own it shows a player nothing — it is what other mods stand on.

***

## 🤔 The problem it solves

A Hytale server holds **one custom page at a time**. Open a second, and the first is dismissed with no warning. So the moment two of your screens need a way back — or two different mods both want a screen — somebody has to remember where the player was.

OpenNavigation is that somebody. It keeps one history per player, and tells whoever owns the screen when to draw.

***

## ✨ What you get

🔙 **A back button that works.** `pop(player)` and the player is where they were. No stack of your own, no page ids to juggle.

🍞 **Breadcrumb trails.** Walk back from where the player stands and you have the trail — with the depth of each step already counted.

🗂️ **Tabs that remember.** A tab is a history of its own. Leave one, come back, and the player is where they left it rather than at the top.

🎒 **A context on every route.** Hang the quest, the shop, the page — whatever that place is about — on the route itself and read it back typed.

🤝 **Several mods, one history.** Routes carry the namespace of the mod that owns them. Going back out of your screen and into another mod's just works, because that mod is the one told to draw it.

🚫 **It never draws.** No frame, no stylesheet, no document of ours to fit into. Your screen stays yours.

✅ **Tested.** The model is plain Java with no server behind it, covered by unit tests that run on every build.

***

## 🧩 How it works

Three calls and an event.

```java
// Open your interface: a fresh history that starts here
NavigationService.get().root(player, new StackRoute("mymod", "shop"));

// Go one level in
NavigationService.get().push(player, new StackRoute("mymod", "shop:item:" + id));

// The back button
NavigationService.get().pop(player);
```

Nothing above opens a page. Each move announces itself, and you draw:

```java
plugin.getEventRegistry().register(RouteChangedEvent.class, "mymod", event -> {
    // event.getRoute() is where the player now stands. Open your page, or redraw it.
});
```

Subscribing on your own namespace means you hear about your routes and nobody else's — and a route no mod claims reaches `registerUnhandled` instead of quietly doing nothing.

***

## 🗺️ Kinds of route

| Type | What it is |
| --- | --- |
| <code>StackRoute</code> | One place, carrying at most one other. The ordinary way forward. |
| <code>TabRoute</code> | Several places at once, one showing. Each tab keeps its own history. |

Both are open to extend, so your route can be a class of your own carrying whatever it needs.

***

## 🛠️ The whole service

`root` · `push` · `pop` · `navigate` · `replace` · `reload` · `setActiveTab` · `search` · `getRoute`

`navigate` goes back to a place already visited and cuts what followed — or pushes it if the player has never been there. That is the breadcrumb click, in one call.

***

## 📦 Installing

Drop the jar in `mods/`. Then declare it in your own `manifest.json`:

```json
"Dependencies": {
  "MartelStudios:OpenNavigation": "*"
}
```

A history lives as long as the session and is never written to disk: where a player stands is a fact about right now, and handing it back after a restart would drop them behind a screen they never opened.

***

## 👀 Seen in the wild

**[OpenQuests](https://www.curseforge.com/hytale/mods/openquests)** uses it for its quest journal: the tabs, the trail across nested quests, and the way back out of a quest you opened from another one.

***

## 📄 License

MIT. Source on [GitHub](https://github.com/MartelStudios/OpenNavigation).
