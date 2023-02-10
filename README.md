# 3D-visualisointi

## Projektin rakentaminen

- Jos käyttää jotakin x64-pohjaista linux-distroa, koodin pitäisi toimia sellaisenaan.
- Jos käyttää Windowsia tai MacOS:ää, pitää build.sbt-tiedostossa vaihtaa `lwjglNatives`-muuttuja vastaamaan omaa käyttöjärjestelmää.
  - Windowsille `"natives-windows"` ja MacOS:lle `"natives-macos"`.
- MacOS:llä ohjelma pitää käynnistää `-XstartOnFirstThread`-vivulla.
