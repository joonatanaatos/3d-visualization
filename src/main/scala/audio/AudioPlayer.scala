package audio

object AudioPlayer {
  val stepSounds: Array[Sound] =
    Array(Sound.Step1, Sound.Step2, Sound.Step3, Sound.Step4, Sound.Step5, Sound.Step6)

  private inline def catchError[T](inline op: T): Unit = {
    try op
    catch {
      case e: RuntimeException => e.printStackTrace()
      case e: Exception        => throw e
    }
  }

  def play(sound: Sound): Unit = catchError { sound.play() }
  def stop(sound: Sound): Unit = catchError { sound.stop() }
  def stop(sounds: Array[Sound]): Unit = sounds.foreach(sound => catchError { sound.stop() })
  def loop(sound: Sound): Unit = catchError { sound.loop() }
  def setVolume(sound: Sound, volume: Float): Unit = catchError { sound.setVolume(volume) }
  def playRandom(sounds: Array[Sound]): Unit =
    catchError { sounds(util.Random.nextInt(sounds.length)).play() }
}
