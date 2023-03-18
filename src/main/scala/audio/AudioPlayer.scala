package audio

object AudioPlayer {
  val stepSounds: Array[Sound] =
    Array(Sound.Step1, Sound.Step2, Sound.Step3, Sound.Step4, Sound.Step5, Sound.Step6)

  def play(sound: Sound): Unit = sound.play()
  def stop(sound: Sound): Unit = sound.stop()
  def stop(sounds: Array[Sound]): Unit = sounds.foreach(_.stop())
  def loop(sound: Sound): Unit = sound.loop()
  def setVolume(sound: Sound, volume: Float): Unit = sound.setVolume(volume)
  def playRandom(sounds: Array[Sound]): Unit =
    sounds(util.Random.nextInt(sounds.length)).play()
}
