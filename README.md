# Minecraft Report Action Bot

Discord bot with **real interactive buttons** (Ban / Tempban / Mute / Kick) for Minecraft player reports.

This is a **separate project** from the webhook report plugin.

It includes:

1. **Discord Bot** (Java + JDA) – handles buttons and talks to your Minecraft server
2. **Companion Minecraft Plugin** – receives the button actions and runs the actual ban/mute/kick commands

---

## How it works

```
Staff clicks [Ban] / [Mute] / [Tempban] button in Discord
        ↓
Discord Bot receives the click
        ↓
Bot sends a secure HTTP request to your Minecraft server
        ↓
Companion plugin executes the punishment command
```

---

## Features

- Real clickable buttons: **Ban**, **Tempban 1d**, **Tempban 7d**, **Mute**, **Kick**
- Works with online & offline players
- Secret key protection
- Clean logging of who punished whom
- Configurable commands (works with Essentials, LiteBans, AdvancedBan, etc.)

---

## Requirements

- Java 17+
- Discord Bot Token
- Paper / Spigot 1.21+
- The companion plugin on your Minecraft server

---

## Setup

### 1. Discord Bot

1. Go to https://discord.com/developers/applications
2. Select your bot application
3. Bot tab → copy the **token**
4. Enable **Server Members Intent** and **Message Content Intent**
5. OAuth2 → URL Generator:
   - Scopes: `bot` + `applications.commands`
   - Permissions: Send Messages, Embed Links, Use Slash Commands
6. Invite the bot to your server

### 2. Companion Plugin (Minecraft)

1. Build the plugin (see below) or use the provided JAR
2. Put `ReportActionPlugin.jar` into `plugins/`
3. Start the server once
4. Edit `plugins/ReportActionPlugin/config.yml`:

```yaml
port: 8123
secret-key: "CHANGE_THIS_TO_A_LONG_RANDOM_STRING"

commands:
  ban: "ban %player% %reason%"
  tempban: "tempban %player% %duration% %reason%"
  mute: "mute %player% %reason%"
  kick: "kick %player% %reason%"
```

5. Make sure port `8123` is reachable from the machine running the Discord bot
6. Restart the server

### 3. Configure & Run the Bot

1. Open `bot/config.yml`
2. Fill in:
   - `bot-token`
   - `secret-key` (same as the plugin)
   - `minecraft-host` (IP of your Minecraft server)
   - `minecraft-port` (8123)
3. Run the bot with Java 17+

---

## Security Warning

- Your bot token was shared in chat. **Reset it** after everything works.
- Use a strong random `secret-key`.
- Prefer firewall rules so only your bot can reach port 8123.

---

## Project Structure

```
MinecraftReportActionBot/
├── bot/                     ← Discord Bot (JDA)
│   ├── src/...
│   ├── pom.xml
│   └── config.yml
├── plugin/                  ← Companion Minecraft Plugin
│   ├── src/...
│   └── pom.xml
└── README.md
```

---

## License

MIT
