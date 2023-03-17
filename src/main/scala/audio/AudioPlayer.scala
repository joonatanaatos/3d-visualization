package audio

object AudioPlayer {
  def play(sound: Sound): Unit = sound.play()
  def stop(sound: Sound): Unit = sound.stop()
  def loop(sound: Sound): Unit = sound.loop()
  def setVolume(sound: Sound, volume: Float): Unit = sound.setVolume(volume)
}
