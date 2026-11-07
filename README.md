# LOOKSMAXXING: From SUB3 to TRUE ADAM — Stage 4 (Forge 1.20.1)

## Build (Windows / Linux / macOS)
1. Install **JDK 17** (e.g. Temurin 17) and **Gradle 8.1.1** (https://gradle.org/releases/).
2. In this folder run:  `gradle build`   (first run downloads Minecraft + Forge, takes several minutes, needs internet)
3. The mod is at `build/libs/looksmaxxing-0.4.0.jar` -> copy it into `.minecraft/mods`.
4. Play with **Forge 1.20.1 (47.2.0 or newer)**.

Alternative: download the official Forge 1.20.1-47.2.0 MDK, and copy the `src/` folder from here over its `src/`
(replace its example code), then run `gradlew build`.

Test in IDE: `gradle genIntellijRuns` / `gradle genEclipseRuns`, then `runClient`.

## What Stage 1 does
- Character creation screen on first join (7 face/body sliders, skin tone, hairstyle, hair color, rotating preview). Data is validated and saved on the server.
- 8 ranks SUB3 -> TRUE ADAM with colors and progress bar; fictional 1-8 rating from a configurable weighted formula.
- HUD (rank, rating, progress, STR/END) and profile screen (key **K**).
- Progression source: sprinting on foot (server-validated, diminishing returns, daily cap).
- Rank-up effect: title, sound, totem particles, server broadcast.
- Rank-Up Token item (creative tab "Looksmaxxing", +3 to all attributes) for testing.
- Stats saved per player, survive death, work on dedicated servers.
- Config: `config/looksmaxxing-common.toml` (weights, training speed, daily cap, rank difficulty).

## Limitations (to be done in next stages)
- Appearance sliders are saved but NOT yet rendered on the player model (Stage 3).
- Only running gives progress; grooming/strength/posture/battle come with Stages 2-4.
  Until battles exist, max overall is ~80, so TRUE CHAD / TRUE ADAM are not reachable yet.
- No custom rank-up animation yet, no gym blocks, no mog battles, no structures.
- Not compiled/tested by the author (no network in the build environment): if Gradle shows an error, send it back.


## Stage 2 additions
- Height and frame sliders now change the real player model (scaled); muscle bulk grows with Strength. Visible to other players too. Skin tone, face and hair are NOT rendered yet.
- Stamina + fatigue system (HUD bars). Overtraining (fatigue 80+) blocks gains. Sleep in a bed to fully recover.
- Items: Resistance Dumbbells, Training Barbell (hold right-click for a set), Pull-Up Bar block (right-click), Protein Meal (+30 stamina, -10 fatigue). All have crafting recipes.
- Daily strength cap, diminishing returns, cooldown (config: training.dailyStrengthCap).
- Animations are vanilla arm swings + particles/sound (real custom animations need GeckoLib, planned for later).
- Not yet: push-ups/squats, mirror, grooming, hammer ritual, mog battles.


## Stage 3 additions
- Real model changes for you and other players: height, frame, muscle (scale) + 6 hairstyles with color, plus nose size, chin, jaw and cheek geometry that uses your own skin texture (may look slightly off on some skins). Jaw/chin get more defined with hammer training.
- Skin tone and eye spacing are saved but not rendered (your custom skin is never recolored).
- Full-Length Mirror block: right-click = edit your look (valid for 1 minute, server-checked); sneak + right-click = posture practice.
- Grooming Kit and Skincare Kit (durability, daily limit, perfume particles) raise Grooming / Facial harmony.
- Jawline Training Hammer + Enchanted Training Dummy: magical ritual (never hits a player). Raises Jawline, Chin, Confidence, Facial harmony; diminishing returns, cooldown, stamina cost, 6 sessions/day, excess = no benefit + stamina loss, 5% rare transformation effect.
- Battle performance is NOT counted in the rating until Mog Arena exists (config weights.battlesEnabled=false), so TRUE ADAM is reachable now with all stats at 100.


## Stage 4: Mog Arena
- Open the menu with **M**, with a **Mog Battle Invitation** (right-click), or by right-clicking a **Mog Arena Platform**.
- 5 battle types: Face-off, Physique, Style, Posture, Overall. 5 rounds each. Every round: pick a pose (Power / Charm / Calm), then LOCK IN (Space) when the moving marker is in the green zone.
  Power beats Calm, Calm beats Charm, Charm beats Power. Poses cost stamina (30/20/10), +15 stamina per round. Round score = half your stat, half your timing accuracy, times a counter bonus.
- Opponents: 6 NPCs (easy/normal/hard, different personalities and pose habits), friendly player challenges, ranked matchmaking queue (ELO), spectating (`/mog spectate <player>`).
- Commands: `/mog accept`, `/mog decline`, `/mog toggle` (turn challenges off), `/mog leaderboard`, `/mog menu`, `/mog spectate <player>`.
- Everything is decided on the server (timing, scores, ELO). Leaderboard is saved per world.
- Rewards: battle performance stat (diminishing returns, daily cap), ELO for ranked, wins/losses/streaks.
- Battle performance now counts in the rating again (weights.battlePerformanceCounts=true) - you need Mog wins to reach TRUE CHAD / TRUE ADAM.
- NPCs are virtual (shown in the battle screen, no 3D entity yet). No camera fly-through yet; the intro and result are animated inside the screen.
