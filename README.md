# BounceCraft

Клиент+серверный мод для **Minecraft 1.21.1 / NeoForge**, вдохновлённый [ViceBounce](https://github.com/tek-95/ViceBounce) для GTA Vice City.

Игрок выбирает на своём компьютере любой трек (OGG / MP3 / WAV). Музыка играет **от этого игрока** (источник двигается вместе с ним). Другие игроки в радиусе (по умолчанию 24 блока) слышат её с затуханием по расстоянию. У всех, кто слышит, живые существа и игроки рядом сплющиваются и растягиваются в такт — только на рендере, физика и хитбоксы не меняются.

modid: `bouncecraft` · версия: `0.1.0`

## English

A NeoForge 1.21.1 client+server mod: pick a local audio file, play it from your player as a positional source, and squash-stretch nearby living entities to the beat (ViceBounce envelope: squash on the beat, stretch at the midpoint). Playback HUD sits at the **top-right**, offset below vanilla status-effect icons when those are shown. Vanilla / no-mod NeoForge clients can still join (payloads are optional; they just will not hear or see the effect). A vanilla client still cannot join a NeoForge server unless that server is vanilla-compatible in other ways.

See **Usage**, **Config**, **Limits**, and **Building** below. Keybinds are rebindable in *Options → Controls → BounceCraft*.

## Установка

Нужны **Java 21** и **NeoForge 1.21.1** (сборка мода против `21.1.262`).

1. Скачайте jar из [Releases](../../releases) или артефакт CI `bouncecraft-jar`.
2. **Клиент:** положите jar в `.minecraft/mods/`.
3. **Сервер:** положите тот же jar в `mods/` dedicated-сервера NeoForge.
4. Мод **опционален для клиентов**: NeoForge-игрок без BounceCraft зайдёт на сервер, где мод стоит, просто не услышит музыку. Vanilla-клиент на чистый NeoForge-сервер обычно всё равно не заходит — это ограничение NeoForge, не реестра BounceCraft (мод не добавляет блоков/предметов/SoundEvent в реестр).

Одиночная игра работает через встроенный сервер.

## Как пользоваться

### Клавиши (переназначаются в Настройки → Управление → BounceCraft)

| Действие | По умолчанию |
|---|---|
| Играть / стоп | `B` — открывает выбор файла, если ничего не играет; останавливает, если играет |
| Выбрать трек | `M` — всегда открывает диалог выбора |
| Эффект вкл/выкл | `H` — только визуал на этом клиенте |

Системный диалог выбора файла — LWJGL TinyFileDialogs. Если он не открылся (на Linux часто нужны `zenity` или `kdialog`; на macOS диалог должен вызываться с главного потока — мы вызываем его из клиентского тика), откроется список файлов из папки:

```
.minecraft/bouncecraft/music/
```

Положите туда `.ogg` / `.mp3` / `.wav` и выберите трек кнопкой «Играть».

Пока играет трек, статус (название, BPM, загрузка) рисуется **в правом верхнем углу**. Если видны иконки зелий vanilla, оверлей сдвигается ниже них.

### Команды

| Команда | Кто | Что делает |
|---|---|---|
| `/bounce stop` | игрок | Останавливает **свой** трек |
| `/bounce bpm <70–180>` | DJ | Пересчитывает сетку битов от первого бита |
| `/bounce reload` | OP (уровень 2) | Печатает текущие значения серверного конфига |

Остановка также происходит, если DJ вышел, умер или сменил измерение.

### Конфиг

Серверный файл (после первого захода в мир):

`world/serverconfig/bouncecraft-server.toml`

| Ключ | По умолчанию | Смысл |
|---|---|---|
| `radius` | 24 | Радиус слышимости и эффекта, блоки |
| `maxTrackBytes` | 10 МиБ | Максимальный размер файла |
| `maxDurationSec` | 600 | Максимальная длительность |
| `uploadCooldownSec` | 30 | Пауза между загрузками на игрока |
| `maxConcurrentDJs` | 8 | Сколько треков одновременно |
| `maxUploadsInFlight` | 2 | Сколько загрузок одновременно на весь сервер |
| `chunksPerTickPerPlayer` | 4 | Сколько чанков (~30 КиБ) слать слушателю за тик |
| `allowedFormats` | ogg, mp3, wav | Разрешённые расширения |
| `requirePermissionLevel` | 0 | Кто может запускать трек (0 = все) |

Клиентский файл: `config/bouncecraft-client.toml`

| Ключ | По умолчанию |
|---|---|
| `effectStrength` | 0.35 (амплитуда squash/stretch, как у ViceBounce, но чуть сильнее) |
| `effectEasing` | 1 (0 linear, 1 sine, 2 quadratic, 3 bounce) |
| `effectEnabled` | true |
| `cacheMaxMB` | 200 |
| `audioDelayMs` | 0 (сдвиг визуального playhead позже, если пик не совпадает со слышимым битом) |

Есть экран конфига NeoForge: Mods → BounceCraft → Config.

## Лимиты и сеть

- Исходный файл уходит на сервер **как есть** (сервер аудио не декодирует) чанками по 30 000 байт (лимит vanilla serverbound payload — 32 767).
- Старт синхронизируется по `level.getGameTime()` сервера с запасом 40 тиков (2 с). Опоздавший слушатель начинает с текущей позиции.
- Биты считает **клиент DJ** (spectral flux + темп 70–180 BPM). Результат рассылается всем.
- Кэш на клиенте по SHA-256: повторно тот же файл не качается.
- Авторские права на треки — ответственность владельца сервера и игроков.

## Сборка из исходников

```bash
./gradlew build
```

Готовый jar: `build/libs/bouncecraft-0.1.0.jar` (JLayer для MP3 упакован через JarJar в `META-INF/jarjar/`).

Тесты чистой логики:

```bash
./gradlew test
```

Headless-сервер (после `eula.txt` / userdev сам принимает EULA):

```bash
./gradlew runServer
```

GameTest:

```bash
./gradlew runGameTestServer
```

## Как выпустить релиз

Не пушьте теги «просто так»: workflow на теге `v*` сам соберёт jar и создаст GitHub Release.

1. Дождитесь зелёного CI на `main` (или смержите PR).
2. Поставьте версию в `gradle.properties` (`mod_version`) при необходимости.
3. Создайте и запушьте тег, совпадающий с версией:

```bash
git tag v0.1.0
git push origin v0.1.0
```

4. В Actions появится сборка; в Releases — jar `bouncecraft-0.1.0.jar`.

Артефакт каждой сборки (push/PR) называется **`bouncecraft-jar`**.

## Известные ограничения

- **Затухание дальше 16 блоков.** OpenAL/Minecraft для *fixed range* режет дистанцию до 16. Мы ставим `attenuation_distance: 24` в `sounds.json` (variable-range). Если на практике звук обрывается раньше — это известный риск; сервер всё равно режет выдачу по своему `radius`.
- **Фаза «бит ↔ картинка».** Визуальный playhead идёт от настенных часов с момента старта (как MCI position у ViceBounce), а не от write-курсора `AudioStream`. Minecraft на `play()` сразу качает ~4×1 с буферов OpenAL — старый курсор прыгал и замирал, из‑за этого эффект выглядел как медленное покачивание. Если пик всё же не на слышимом бите, крутите `audioDelayMs`.
- **Синхронизация времени** клиента с сервером обновляется пакетом раз в секунду (~50–100 мс плюс джиттер).
- **TinyFileDialogs** на Linux без zenity/kdialog и на macOS вне главного потока может не открыться — есть фолбэк-папка `bouncecraft/music/`.
- **Сломанный файл:** декод в фоне, клиент не должен падать; HUD покажет ошибку.
- **Vanilla-клиент** на NeoForge-сервер сам по себе не заходит. Опциональные payload'ы нужны, чтобы *NeoForge-клиент без этого мода* не отваливался на handshake.
- MP3 декодируется **JLayer 1.0.1** (LGPL-2.1), исходники библиотеки — Maven Central.

## Лицензия

Код мода — MIT (`LICENSE`). Кривая пульса (фаза битов, easing, squash/stretch) портирована из [ViceBounce](https://github.com/tek-95/ViceBounce) (MIT, tek-95) — см. `NOTICE`. JLayer — LGPL-2.1 (`licenses/`).
