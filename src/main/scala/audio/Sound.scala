package audio

import java.io.BufferedInputStream
import javax.sound.sampled.{AudioSystem, Clip, FloatControl}

enum Sound(val name: String) {
  case BackgroundMusic extends Sound("background-music")
  case Demon extends Sound("demon")

  case Step1 extends Sound("steps/step-01")
  case Step2 extends Sound("steps/step-02")
  case Step3 extends Sound("steps/step-03")
  case Step4 extends Sound("steps/step-04")
  case Step5 extends Sound("steps/step-05")
  case Step6 extends Sound("steps/step-06")

  private val (clipOption, gainControlOption) = loadAudio()

  @throws[RuntimeException]
  private def getClip: Clip =
    clipOption.getOrElse(throw new RuntimeException(s"Failed to load audio $name"))

  @throws[RuntimeException]
  private def getGainControl: FloatControl =
    gainControlOption.getOrElse(throw new RuntimeException(s"Failed to load audio $name"))

  private def loadAudio(): (Option[Clip], Option[FloatControl]) = {
    try {
      val path = s"/audio/$name.wav"
      val audioStream = new BufferedInputStream(getClass.getResourceAsStream(path))
      val audio = AudioSystem.getAudioInputStream(audioStream)
      val clip = AudioSystem.getClip()
      clip.open(audio)
      val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl]
      (Option(clip), Option(gainControl))
    } catch {
      case e: IllegalArgumentException =>
        System.err.println(s"Failed to load audio $name")
        e.printStackTrace()
        (None, None)
      case e: Exception => throw e
    }
  }

  @throws[RuntimeException]
  protected[audio] def play(): Unit = {
    val clip = getClip
    if clip.isActive then this.stop()
    clip.setFramePosition(0)
    clip.start()
  }

  @throws[RuntimeException]
  protected[audio] def stop(): Unit = {
    val clip = getClip
    clip.stop()
    clip.flush()
    // FIXME: This is a hack to prevent the audio from getting stuck
    Thread.sleep(1)
  }

  @throws[RuntimeException]
  protected[audio] def loop(): Unit = {
    val clip = getClip
    if clip.isActive then this.stop()
    clip.setFramePosition(0)
    clip.loop(Clip.LOOP_CONTINUOUSLY)
  }

  @throws[RuntimeException]
  protected[audio] def setVolume(volume: Float): Unit = {
    val gainControl = getGainControl
    gainControl.setValue(volume)
  }

  @throws[RuntimeException]
  protected[audio] def isPlaying: Boolean = getClip.isActive
}
