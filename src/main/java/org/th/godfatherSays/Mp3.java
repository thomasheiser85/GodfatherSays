package org.th.godfatherSays;

import java.io.IOException;

public class Mp3
{
  // default -1500
  public static String VOLUME = "-1500";

  private static final String OS_COMMAND = "omxplayer";
  private static final String[] OS_KILL_COMMAND = { "killall", "omxplayer.bin" };
  private String fileName;
  private Process process;

  public Mp3(String fileName)
  {
    this.fileName = fileName;

  }

  public void play()
  {
    String command = OS_COMMAND + "\"" + fileName + "\"";
    try
    {
      System.out.println("MP3, executing os command: " + command);
      // process = Runtime.getRuntime().exec(new String[] { "bash", "-c",
      // command });
      process = Runtime.getRuntime().exec(new String[] { OS_COMMAND, "-b", "--vol", VOLUME, fileName });

      // try
      // {
      // final BufferedReader reader = new BufferedReader(new
      // InputStreamReader(process.getInputStream()));
      // String line = null;
      // while ((line = reader.readLine()) != null)
      // {
      // System.out.println(line);
      // }
      // reader.close();
      // } catch (final Exception e)
      // {
      // e.printStackTrace();
      // }
      process.waitFor();
      // while (process.isAlive())
      // {
      // Thread.sleep(500);
      // }

    } catch (IOException e)
    {
      e.printStackTrace();
      System.out.println("Unable to play mp3: " + command);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  public void stop()
  {
    if (process != null)
    {
      System.out.println("KILL MP3 process");
      process.destroy();
      try
      {
        Runtime.getRuntime().exec(OS_KILL_COMMAND);
      } catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }
}