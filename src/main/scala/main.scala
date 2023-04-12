import engine.Engine
import game.Game

@main
def main(): Unit = {
  val game = new Game()
  val engine = new Engine(60, Engine.UNLIMITED, game)
  engine.start()
}
