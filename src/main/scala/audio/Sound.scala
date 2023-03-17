package audio

import javax.sound.sampled.{AudioSystem, Clip, FloatControl}

enum Sound(val name: String) {
  case BackgroundMusic extends Sound("background-music")

  private val (clip, gainControl) = loadAudio()

  private def loadAudio(): (Clip, FloatControl) = {
    try {
      val path = s"/audio/$name.wav"
      val audioStream = getClass.getResourceAsStream(path)
      val audio = AudioSystem.getAudioInputStream(audioStream)
      val clip = AudioSystem.getClip()
      clip.open(audio)
      val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl]
      (clip, gainControl)
    } catch {
      case e: Exception => throw new RuntimeException(s"Failed to load audio $name", e)
    }
  }

  protected[audio] def play(): Unit = {
    if clip.isActive then this.stop()
    clip.setFramePosition(0)
    clip.start()
  }

  protected[audio] def stop(): Unit = {
    clip.stop()
    clip.flush()
  }

  protected[audio] def loop(): Unit = {
    if clip.isActive then this.stop()
    clip.setFramePosition(0)
    clip.loop(Clip.LOOP_CONTINUOUSLY)
  }

  protected[audio] def setVolume(volume: Float): Unit = {
    gainControl.setValue(volume)
  }
}
