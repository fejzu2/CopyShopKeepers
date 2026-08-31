# CopyShopKeepers

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-3d9c40?style=flat-square)
![Fabric](https://img.shields.io/badge/Loader-Fabric-3d5a99?style=flat-square)
![Side](https://img.shields.io/badge/Side-Client--only-9c5cff?style=flat-square)
![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-lightgrey?style=flat-square)

**Otwórz handel z wieśniakiem, wciśnij `Ctrl + C`, wklej gotowy fragment do `save.yml` pluginu ShopKeepers.** Zero przepisywania NBT-ów, zero liczenia wcięć ręcznie.

---

## Spis treści

- [Co to robi](#co-to-robi)
- [Funkcje](#funkcje)
- [Wymagania](#wymagania)
- [Instalacja](#instalacja)
- [Użycie](#użycie)
- [Komendy](#komendy)
- [Format `shopkeepers` — jak wkleić do save.yml](#format-shopkeepers--jak-wkleić-do-saveyml)
- [Konfiguracja](#konfiguracja)
- [Jak to działa (bez moda na serwerze)](#jak-to-działa-bez-moda-na-serwerze)
- [Struktura projektu](#struktura-projektu)
- [Budowanie ze źródeł](#budowanie-ze-źródeł)
- [Licencja](#licencja)

---

## Co to robi

CopyShopKeepers to **kliencki** mod do Fabric, który czyta oferty handlu z aktualnie otwartego okna merchanta (zwykły wieśniak albo shopkeeper z pluginu [ShopKeepers](https://github.com/Shopkeepers/Shopkeepers)) i eksportuje je do schowka w jednym z trzech formatów — w tym w formacie **gotowym do wklejenia 1:1** w plik `save.yml` pluginu ShopKeepers, z poprawnym kluczem (`recipes:`/`offers:`), realnym `DataVersion` i wcięciami dopasowanymi do reszty pliku.

Powstał, żeby skończyć z ręcznym przepisywaniem `resultItem` / `item1` / `item2`, kodów NBT lore/enchantów i liczenia spacji za każdym razem, gdy ktoś stawia nowy sklep administracyjny.

## Funkcje

- ⌨️ **`Ctrl + C`** działa nawet z otwartym oknem handlu (Minecraft normalnie blokuje inne skróty przy otwartym GUI — ten mod to obchodzi na poziomie zdarzeń klawiatury ekranu).
- 🧾 **Trzy formaty eksportu**: `readable` (czytelny podgląd), `shopkeepers` (fragment YAML do save.yml), `json` (pełny zrzut NBT→JSON każdego itemu).
- 🏪 **Dwa typy sklepu**: `admin` (`recipes:`) i `trading` (`offers:`) — mod sam dobiera właściwy klucz zamiast zgadywać.
- 🔢 **Prawdziwy `DataVersion`**, pobierany w locie z uruchomionej wersji gry (`SharedConstants.WORLD_VERSION`), nie wpisany na sztywno.
- 📐 **Wcięcia dopasowane pod strukturę pliku** — wynik można wkleić jako pełną zamianę linii `recipes:`/`offers:` bez poprawiania choćby jednej spacji.
- 💾 **Opcjonalny zapis do pliku** (`.minecraft/villager-export/`) z klikalną ścieżką w czacie, która otwiera plik.
- 🎨 **Kolorowe komunikaty** z gradientem RGB, sterowane z zewnętrznego `messages.json` — żadnego tekstu na sztywno w kodzie.
- ⚙️ **Prosty config JSON**, edytowalny ręcznie, bez wymaganego Mod Menu / Cloth Config.
- 🌍 **Działa na dowolnym vanilla serwerze** — to komenda wyłącznie kliencka, serwer (poza samym pluginem ShopKeepers, do którego wklejasz) niczego nie potrzebuje.

## Wymagania

| Zależność | Wersja |
|---|---|
| Minecraft | `1.21.11` |
| Fabric Loader | `≥ 0.19.3` |
| Fabric API | `0.141.6+1.21.11` |

## Instalacja

1. Zainstaluj [Fabric Loader](https://fabricmc.net/use/) dla Minecrafta `1.21.11`.
2. Wrzuć [Fabric API](https://modrinth.com/mod/fabric-api) do folderu `mods`.
3. Wrzuć tam też `CopyShopKeepers-*.jar`.
4. Uruchom grę — mod jest wyłącznie kliencki, nic więcej nie trzeba instalować.

## Użycie

1. Otwórz handel z wieśniakiem albo shopkeeperem (dowolny serwer, dowolny plugin).
2. Wciśnij **`Ctrl + C`** (domyślny bind, zmienisz go w *Ustawienia → Sterowanie → CopyShopKeepers*).
3. Trade'y lądują w schowku w domyślnym formacie (`shopkeepers` na start) — wklej gdzie potrzeba.

Albo użyj komendy `/villagerexport`, jeśli wolisz jawnie wybrać format za każdym razem.

## Komendy

| Komenda | Opis |
|---|---|
| `/villagerexport` | Kopiuje trade'y domyślnym formatem i typem sklepu |
| `/villagerexport save` | jw. + dodatkowo zapisuje plik na dysk |
| `/villagerexport readable [save]` | Czytelna lista trade'ów (podgląd, nie do wklejenia) |
| `/villagerexport json [save]` | Pełny zrzut JSON każdego itemu (`id`/`count`/`components`) |
| `/villagerexport shopkeepers [admin\|trading] [save]` | Fragment YAML gotowy do wklejenia w `recipes:`/`offers:` |
| `/villagerexport format <readable\|shopkeepers\|json>` | Zmienia domyślny format na stałe |
| `/villagerexport shoptype <admin\|trading>` | Zmienia domyślny typ sklepu na stałe |
| `/villagerexport help` | Lista komend w grze |

## Format `shopkeepers` — jak wkleić do save.yml

ShopKeepers zapisuje oferty pod **różnym kluczem w zależności od typu shopkeepera**:

| Typ sklepu (`type:` w save.yml) | Klucz z ofertami | Kiedy wybrać |
|---|---|---|
| `admin` | `recipes:` | Sklep administracyjny, nieskończona podaż (najczęstszy przypadek) |
| `trading` | `offers:` | Sklep gracza zasilany ze skrzynki, wymiana item-za-item |

> Sklepy `selling`/`buying` (sprzedaż/skup za konkretną cenę) mają zupełnie inny układ danych (pojedynczy item + cena, nie `resultItem`/`item1`/`item2`) i **nie są obsługiwane** przez ten eksport — on 1:1 odwzorowuje okno handlu (do 2 itemów kupujących + 1 sprzedający).

Wynik komendy `/villagerexport shopkeepers` jest już wcięty tak, żeby pasował pod istniejącą strukturę shopkeepera. Zaznacz **całą linię** `recipes:` (albo `recipes: {}`) u wybranego shopkeepera w `save.yml` i wklej w to miejsce — bez poprawiania spacji:

```yaml
'1':
  uniqueId: cd6ec952-42b7-4f0d-b0ba-8bf1b782dde4
  type: admin
  # ...
  recipes:          # <- ta linia, zaznacz i zamień na wklejony blok
    "1":
      resultItem:
        DataVersion: 4903
        id: minecraft:emerald
        count: 1
      item1:
        DataVersion: 4903
        id: minecraft:diamond
        count: 3
  snapshots: []
```

`DataVersion` w wygenerowanym pliku to wersja danych **Twojej aktualnie uruchomionej gry** — eksportuj i wklejaj na serwerze działającym na tej samej (albo zbliżonej) wersji Minecrafta.

## Konfiguracja

Config generuje się automatycznie przy pierwszym uruchomieniu: `.minecraft/config/copyshopkeepers.json`.

```json
{
  "defaultFormat": "shopkeepers",
  "defaultShopType": "admin",
  "alsoSaveToFile": true
}
```

| Pole | Wartości | Opis |
|---|---|---|
| `defaultFormat` | `readable` / `shopkeepers` / `json` | Format używany przez `Ctrl+C` i samo `/villagerexport` |
| `defaultShopType` | `admin` / `trading` | Typ sklepu używany przy formacie `shopkeepers` |
| `alsoSaveToFile` | `true` / `false` | Czy `Ctrl+C` ma też zapisywać plik do `.minecraft/villager-export/` |

Oba pola `defaultFormat`/`defaultShopType` można też zmieniać w grze komendami `/villagerexport format` i `/villagerexport shoptype` — config zapisuje się od razu.

## Jak to działa (bez moda na serwerze)

Mod jest zarejestrowany jako `"environment": "client"` i korzysta wyłącznie z **klienckiego** API Fabric (`ClientCommandManager`, `ScreenKeyboardEvents`) — czyta ofertę handlu bezpośrednio z lokalnego `MerchantScreenHandler`, czyli danych, które klient i tak już ma od serwera po otwarciu okna handlu. Dzięki temu działa identycznie na dowolnym vanilla serwerze, modowanym serwerze czy w singleplayerze — serwer nie musi mieć żadnego moda, wystarczy że masz otwarte okno handlu.

## Struktura projektu

```
src/main/java/.../FabricMod.java                  – entrypoint main (server+client)
src/client/java/.../client/
  FabricModClient.java                             – entrypoint client, spina wszystko
  command/ExportCommand.java                        – /villagerexport i jego subkomendy
  keybinding/CopyKeybinding.java                    – Ctrl+C w oknie handlu
  config/ExportConfig.java, ShopType.java           – config JSON, typy sklepu
  export/VillagerExportService.java                 – budowanie 3 formatów eksportu
  export/ItemNbtCodec.java                           – kodowanie ItemStack -> NBT/JSON
  export/ExportFeedback.java                         – wspólna wiadomość zwrotna czatu
  util/Messages.java, ColorCodes.java                – messages.json + kolory &/&#RRGGBB
src/client/resources/assets/copyshopkeepers/messages/messages.json  – wszystkie teksty czatu
```

## Budowanie ze źródeł

```bash
./gradlew build
```

Zbudowany jar wyląduje w `build/libs/`.

## Licencja

All Rights Reserved — zobacz [LICENSE.txt](LICENSE.txt).
