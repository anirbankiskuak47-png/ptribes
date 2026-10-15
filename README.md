# Prehistoric Tribes – New Horizons (Android, landscape, touch)

## APK banane ka tareeka (GitHub, free)
1. GitHub par naya repo banao, is poore folder ki files usme upload karo (`.github` folder bhi).
2. Repo me **Actions** tab kholo -> "Build APK" chalne do (2-4 min).
3. Run khulne par neeche **Artifacts -> PrehistoricTribes-debug-apk** download karo, zip kholo, `app-debug.apk` phone me install karo.
(Android Studio hai to folder ko seedha open karke Build > Build APK bhi ho jayega.)

## Touch controls (Clash of Clans / Age of Empires style)
- **Drag (ek ungli)**: map ko khiskao.
- **Tap**: select / order (villager select -> zameen, ped, patthar, building par tap = kaam).
- **Long-press + drag**: group select ka box (AoE jaisa). Ungli uthate hi group select.
- **Do ungli se ek saath tap**: cancel / deselect.
- **Floating buttons**: MENU (build/commands), BACK (cancel/pause), 7 / 9 (control groups, dabake rakho = save), ! (alert par jao), MAP, ? (help).
- **Menus me**: upar/neeche swipe = highlight move, tap = OK. Neeche ke corner ke game ke icons (tick / cross) par tap bhi chalta hai.
- Android **Back button** = game ka cancel/pause key. Exit ke liye game ke main menu me "Exit".

## Pata rahe
- Game ka map-view abhi bhi 240x320 (portrait) hai jo landscape screen ke beech me bada dikhta hai. Zyada chauda map (widescreen) alag, agla kaam hai.
- Pehli baar test: agar black screen aaye to `adb logcat -s PT` ka output bhejna.
