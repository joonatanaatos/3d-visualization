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

## 20.3.2023

Ensimmäinen isompi kokonaisuus, jonka olen saanut toteutettua, on 3D-perspektiiviprojektio.
Siihen sisältyi teknisessä suunnitelmassa kuvatun algoritmin toteuttaminen OpenGL:n rajapinnan avulla.

Projektiin on lisätty seuraavat luokat:
- game/Menu
- game/GameState
- logic/Light
- logic/Demon
- logic/GameObject
- audio/AudioPlayer
- audio/Sound

Lähes kaikkia luokkia on päivitetty.

Seuraavat toiminnallisuudet on toteutettu:
- Valaistusmoottori / vilkkuvat kattolamput
- Äänimoottori / itsesävelletty taustamusiikki ja muut peliäänet
- Pelaajaa seuraava demoni
- Erikoistehosteita (mm. näytön tärinä)
- Pientä renderöinnin optimointia
- Pelivalikko

Testaus:
- Olemassa olevia yksikkötestejä on päivitetty vastaamaan uutta koodia.
- Kaikkia uusia ohjelman osia on testattu manuaalisesti.

Kehityksessä esiintyneitä haasteita:
- Valaistusmoottorin toteuttamisessa esiintyi paljon ongelmia, joista suurin osa lopulta ratkesi.
  En kuitenkaan onnistunut tunnistamaan, milloin jonkin pisteen ja valonlähteen välissä on este.
  Tämän seurauksena kattolamput valaisevat myös seinien takana olevia pintoja.
- Valikon lisäämisen myötä ohjelmalla voi olla monta eri tilaa.
  Näiden hallinnointi on tuottanut haasteita, ja päädyin lopulta toteuttamaan vähän kömpelkön ratkaisun.
  Ratkaisu ei ole kovin laajennettava, ja en oikein tiedä, miten toivomani toiminnallisuus olisi paras toteuttaa.
- Äänimoottorin kanssa oli sellainen ongelma, että välillä jotkin äänet eivät soineet ollenkaan.
  Tämä toistui erityisesti pelaajan askelten ääniä soittaessa.
  Löysin väliaikaiseksi ratkaisuksi odottaa vähän aikaa äänen pysäyttämisen jälkeen ennen uuden äänen soittamista.
  Ratkaisu ei ole optimaalinen ja haluaisin löytää jonkin paremman ratkaisun,
  mutta on kuitenkin epätodennäköisetä, että se vaikuttaisi mitenkään ohjelman suorituskykyyn.
- Pelin ääniä soittava koodi on toteutettu kaiken muun toiminnallisuuden seassa.
  Haluaisin, että kaikki äänien soittamiseen liittyvä toiminnallisuus toteutettaisiin omassa kokonaisuudessaan,
  mutta en keksinyt mitään järkevää ratkaisua tähän.
  Minun ajatus on, että `logic`-pakkaus voisi tarjota rajapinnan, jolla voi luoda tapahtumankuuntelijoita äänen soittamista varten.
  Tätä varten pitäisi myös laukausita niitä tapahtumia jotenkin `logic`-pakkauksesta käsin,
  mutta en tiedä, miten se tehdään niin, että koodi säilyy ylläpidettävänä.
- Jostain syystä CI-putkessa äänitiedostojen lataaminen ei onnistunut. Ongelma ei ratkennut.

Projektiin on käytetty noin 25 tuntia lisää aikaa ja siinä aletaan menemään ohi aiheen.
Olen käyttänyt tähän projektiin jo liikaa aikaa ja minun pitäisi määrittää, milloin se on "valmis".
