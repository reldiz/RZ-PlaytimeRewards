<div align="center">

# 🎁 RZ-PlaytimeRewards

**A modern, lightweight, and fully customizable Minecraft playtime rewards plugin.**

[![Version](https://img.shields.io/badge/version-1.21-brightgreen.svg)]()
[![Author](https://img.shields.io/badge/author-Rokas-blue.svg)]()
[![Platform](https://img.shields.io/badge/platform-Spigot%20%2F%20Paper-orange.svg)]()

</div>

---

### 🌟 Features

* **Interactive GUI Menu:** Clean, multi-page inventory interface for players to claim their rewards.
* **Custom Action Tags:** Execute actions effortlessly upon claim:
  * `[console]` – Execute server commands.
  * `[message]` – Send private messages directly to the player.
  * `[broadcast]` – Announce claims server-wide.
* **Placeholder Support:** Built-in `%player%` support across all actions and messages.
* **Soft Dependencies:** Full compatibility with **CMI** and **Essentials** for playtime tracking.
* **100% Configurable:** Customize item names, lores, time requirements (in seconds), and color codes (`&`).

---

### 📜 Commands & Permissions

Primary Command: `/playtime`  
**Aliases:** `/playtimerewards`, `/rewards`, `/ptr`, `/pta`, `/playtimeadmin`

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/playtime` | Opens the Playtime Rewards GUI | `rzplaytimerewards.use` |
| `/playtime reload` | Reloads all configuration files | `rzplaytimerewards.admin` |

---

### ⚙️ Configuration

#### `rewards.yml`
```yaml
rewards:
  reward_1:
    display-name: "30 Minutes Playtime"
    playtime-required: 1800 # Time in seconds (1800s = 30m)
    description:
      - "&7- &f$500 Cash"
      - "&7- &f16x Iron Ingots"
    commands:
      - "[console] eco give %player% 500"
      - "[console] give %player% iron_ingot 16"
      - "[message] &a[Rewards] &fYou successfully claimed the 30 Minutes reward!"
      - "[broadcast] &e%player% &7has claimed the &e30 Minutes Playtime &7reward!"
