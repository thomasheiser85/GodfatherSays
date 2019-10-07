package org.th.godfatherSays;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class GameEngine
{
  private GPIOService gpio;

  // static variables will be set during startup
  static int START_LEVEL;
  static int START_LED_TIME;
  static int LED_OFF_PERCENT;
  static int ROUND_FASTER;
  static long BUTTON_FAILSAFE_MILLISECONDS;
  static int BUTTON_TRIGGER_LED_TIME;
  static boolean BUTTON_TRIGGER_UP;

  static int NO_OF_CONTROLS;
  static String[] BUTTON_LIST;
  static String[] LED_LIST;

  public static boolean BUTTON_SOUND;

  private int currentLedTime;
  private int currentLedOffTime;
  private int currentLevel;
  private ConcurrentLinkedQueue<Integer> currentQueue;

  private ArrayList<Pin> leds;
  private ArrayList<Pin> buttons;

  private long failsaveTimestamp = 0;
  private int failsaveButton = 0;

  private boolean enableInputs = true;

  public GameEngine()
  {
    System.out.println("Starting game engine ...");

    gpio = GPIOService.getInstance();

    leds = new ArrayList<Pin>();
    buttons = new ArrayList<Pin>();

    for (int i = 0; i < NO_OF_CONTROLS; i++)
    {
      leds.add(RaspiPin.getPinByName(LED_LIST[i]));

      Pin button = RaspiPin.getPinByName(BUTTON_LIST[i]);
      buttons.add(button);

      int buttonIndex = i + 1;

      gpio.addButtonListener(button, new GpioPinListenerDigital()
      {
        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
        {
          if (BUTTON_TRIGGER_UP && event.getState().isHigh())
          {
            // on button up
            buttonTrigger(buttonIndex);
            return;
          }
          if (!BUTTON_TRIGGER_UP && event.getState().isLow())
          {
            // on button down
            buttonTrigger(buttonIndex);
            return;
          }
        }
      });
    }
  }

  public void start()
  {
    System.out.println("Starting a new game ...");

    currentLevel = START_LEVEL - 1;
    currentLedTime = START_LED_TIME;
    currentLedOffTime = (currentLedTime * LED_OFF_PERCENT) / 100;
    nextLevel();

  }

  private void nextLevel()
  {
    enableInputs = false;

    currentLevel++;
    currentQueue = new ConcurrentLinkedQueue<Integer>();

    // add random integer to queue
    for (int i = 0; i < currentLevel; i++)
    {
      currentQueue.offer(getRandomButton());
    }

    System.out.println("Level " + currentLevel + " -> " + currentQueue);

    playback();

    currentLedTime -= (currentLedTime * ROUND_FASTER) / 100;
    currentLedOffTime = (currentLedTime * LED_OFF_PERCENT) / 100;

    enableInputs = true;
  }

  private void playback()
  {
    ledOff();

    Integer[] playbackButtons = currentQueue.toArray(new Integer[0]);
    for (Integer integer : playbackButtons)
    {
      sleep(currentLedOffTime);

      if (BUTTON_SOUND)
      {
        try
        {
          SoundCache.BUTTON_SOUNDS.get(integer - 1).play();
        } catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      ledPulse(integer, currentLedTime);
    }

    SoundCache.AWAITING_INPUT.play();
    System.out.println("Waiting for user inputs ...");
    ledOff();
  }

  private void ledOn()
  {
    for (Pin led : leds)
    {
      ledOn(led);
    }
    System.out.println("LED ON");
  }

  private void ledOff()
  {
    for (Pin led : leds)
    {
      ledOff(led);
    }
    System.out.println("LED OFF");
  }

  private void ledOn(Pin led)
  {
    gpio.ledOn(led);
  }

  private void ledOff(Pin led)
  {
    gpio.ledOff(led);
  }

  private void ledPulse(int led, int time)
  {
    ledPulse(leds.get(led - 1), time);
  }

  private void ledPulse(Pin led, int time)
  {
    System.out.println("LED [" + led + "] for [" + time + "] ms!");

    gpio.ledOn(led, time, true);
  }

  private int getRandomButton()
  {
    return ThreadLocalRandom.current().nextInt(1, NO_OF_CONTROLS + 1);
  }

  public void buttonTrigger(int button)
  {
    if (!enableInputs)
    {
      System.out.println("Button [" + button + "] -> Inputs currently disabled... ignore!");
      return;
    }

    // check for double click or input problems
    long timestamp = System.currentTimeMillis();
    if (failsaveButton == button && (failsaveTimestamp > timestamp - BUTTON_FAILSAFE_MILLISECONDS))
    {
      System.out.println("Button [" + button + "] triggered too fast... ignore!");
      return;
    }
    failsaveButton = button;
    failsaveTimestamp = timestamp;

    System.out.println("Button [" + button + "] triggered!");
    Integer suggestedButton = currentQueue.poll();
    if (suggestedButton == null)
    {
      System.out.println("Queue is empty, button will be ignored!");
      return;
    } else if (suggestedButton == button)
    {
      System.out.println("=========================================> HIT");

      // flicker led
      ledPulse(button, BUTTON_TRIGGER_LED_TIME);

      if (currentQueue.isEmpty())
      {
        win();
      } else
      {
        System.out.println("Level " + currentLevel + " -> " + currentQueue);
        SoundCache.BUTTON_PRESS.play();
      }
    } else
    {
      lose();
    }

  }

  private void lose()
  {
    ledOn();
    System.out.println("=========================================> FAIL");
    SoundCache.FAIL.play();
    sleep(SoundCache.FAIL.getMillisecondLength());
    start();
  }

  private void win()
  {
    ledOn();
    System.out.println("Level " + currentLevel + " finished!");
    SoundCache.WIN.play();
    sleep(SoundCache.WIN.getMillisecondLength());

    // TODO Save highscore and additional sound playback?

    nextLevel();
  }

  private void sleep(long millis)
  {
    try
    {
      System.out.println("Sleep: " + millis);
      Thread.sleep(millis);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  public void destroy()
  {
    gpio.shutdown();
  }

}
