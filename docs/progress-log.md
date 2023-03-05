# Projektin eteneminen

## 6.3.2023

Projekti on lähtenyt alkuun erittäin hyvin.

Projektiin on lisätty seuraavat luokat:
- game/Game
- engine/EngineInterface
- engine/GameInterface
- engine/Engine
- graphics/Renderer
- graphics/Utils
- graphics/Texture
- graphics/Window
- graphics/ShaderProgram
- graphics/RenderingHelper
- logic/World
- logic/EventListener
- logic/Player
- logic/Direction
- logic/Stage

Edellisissä luokissa on toteutettu seuraavat toiminnallisuudet:
- GLFW-ikkuna ja -tapahtumankuuntelijat
- OpenGL-renderöinti
- 3D-perspektiiviprojektio
- Tekstuurimäppäys
- Pelaajan liike ja törmäyksentunnistus
- Maailman lataaminen yml-tiedostosta

Testaus:
- Kaikkia ohjelman osia on testattu manuaalisesti ja ne toimivat oikein.
- Yksikkötestejä on kirjoitettu `logic`-pakkaukselle, mutta ne testaavat luokkien toiminnallisuutta vain yleisellä tasolla.
- Yksikkötestit ajetaan automaattisesti projektin CI-putkessa, millä varmistetaan, ettei perustoiminnallisuudet mene rikki.

Kehityksessä esiintyneitä haasteita:
- Varjostimien lataaminen ei aluksi toiminut, koska annoin `Int`-tyyppiset parametrit väärin päin.
- En saanut CI-putkessa ajettua Scalafmt:tä, mutta tämä ratkesi lopulta Otto Seppälän avustuksella.
- OpenGL-virheiden seuraamisessa olen joutunut käyttämään `glCheck`-apufunktiota, mikä ei ole ihan optimaalista.

Projektiin on käytetty noin 25 tuntia aikaa ja sen minimivaatimukset on täytetty.
Käyttämäni aika vastaa projektisuunnitelmassa määritettyä haarukkaa.
