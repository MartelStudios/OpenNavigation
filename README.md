# OpenNavigation

Routed navigation for Hytale interfaces. A player stands on a **route**; routes remember what they
came from, so going back is reading that history rather than rebuilding it.

The model is ported from [UEX Navigation](https://github.com/UExtension), where a route is a node in
a linked history — it knows its previous, its next and its depth — and the service is the only thing
that moves the player between them. Nothing in that shape is Unity's: a scene becomes a page, and
the rest carries over.

## Status

Early. The project is scaffolded and the model is being ported.

## Building

```
./gradlew build
```

The jar lands in `build/libs/`. `./gradlew runServer` starts a dev server with the mod staged into
it — one plugin, so there is no workspace to run several of them.

## Releasing

The **Release** workflow, run from the Actions tab, writes the version into `gradle.properties`,
tags it and publishes the jar. It needs the `RELEASE_APP_ID` and `RELEASE_APP_PRIVATE_KEY` secrets.
Publishing to CurseForge is not wired yet — that comes with the first release, once the project has
an id there.

## License

MIT.
