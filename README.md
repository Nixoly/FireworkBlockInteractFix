<div align="center">

<img src="https://i.imgur.com/nfVy6Ei.png" alt="image" />

<h1>FireworkBlockInteractFix</h1>

</div>

<div align="center">

While gliding, Minecraft is supposed to stop you from using a firework rocket when something in front of you would block it—typically a block you are looking at, or awkward cases like your hitbox clipping a cobweb while your crosshair is still on a real block. Those checks exist so you do not get a rocket boost through a situation where the item should not “go off” like that.

Modded clients can ignore that and launch anyway. This plugin runs the same idea on the server: if the player should have been blocked by those conditions, the rocket launch is undone (position, motion, entity cleanup) so the thrust never sticks.

</div>

## Requirements

- Spigot or Paper **1.16+** (API version in `plugin.yml` is 1.16)
- Java **8** or newer on the server

PacketEvents is **bundled** in the shaded jar from this repo—you do not install PacketEvents separately unless you build a non-shaded variant yourself.

## Build

```bash
./gradlew build
```

The output jar is copied to `buildJar/` at the end of the build.

## Install

Download the latest `FireworkBlockInteractFix-x.x.x.jar` from **Releases** on this repository, place it in your server’s `plugins/` directory, then restart the server.

## Debugging

Developers can flip `Tunables.DEBUG` to `true` in the source for extra console lines while testing. Turn it off for releases.
