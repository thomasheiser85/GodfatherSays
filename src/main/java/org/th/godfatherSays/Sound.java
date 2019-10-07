package org.th.godfatherSays;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Sound
{
  private Clip clip;

  private long millisecondLength;

  public Sound(String fileName)
  {
    System.out.println("Loading sound: " + fileName);

    // specify the sound to play
    // (assuming the sound can be played by the audio system)
    // from a wave File
    try
    {
      // load file from resource
      URL url = this.getClass().getClassLoader().getResource(fileName);
      AudioInputStream sound = AudioSystem.getAudioInputStream(url);

      // load the sound into memory (a Clip)
      clip = AudioSystem.getClip();
      clip.open(sound);

      try
      {
        FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        volume.setValue(6f);
      } catch (Exception e)
      {
        e.printStackTrace();
      }

      millisecondLength = clip.getMicrosecondLength() / 1000;

    } catch (MalformedURLException e)
    {
      e.printStackTrace();
      System.out.println("Sound: Malformed URL: " + e);
    } catch (UnsupportedAudioFileException e)
    {
      e.printStackTrace();
      System.out.println("Sound: Unsupported Audio File: " + e);
    } catch (IOException e)
    {
      e.printStackTrace();
      System.out.println("Sound: Input/Output Error: " + e);
    } catch (LineUnavailableException e)
    {
      e.printStackTrace();
      System.out.println("Sound: Line Unavailable Exception Error: " + e);
    }

    // play, stop, loop the sound clip
  }

  public void play()
  {
    if (clip.isRunning())
    {
      clip.stop(); // Stop the player if it is still running
    }
    clip.setFramePosition(0); // Must always rewind!
    clip.start();
  }

  public void loop()
  {
    clip.loop(Clip.LOOP_CONTINUOUSLY);
  }

  public void stop()
  {
    clip.stop();
  }

  public long getMillisecondLength()
  {
    return millisecondLength;
  }
}