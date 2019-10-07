package org.th.godfatherSays;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Menu implements Runnable
{

    private static final String MUSICBOX = "MusicBox";
    private static final String GODFATHERSAYS = "GodfatherSays";
    // boolean musicBoxActive = false;

    private GameEngine gameplay = null;
    private MusicBox musicBox = null;

    private boolean isAlive = true;
    public long lastUp;
    public long lastDown;

    private GPIOService gpio;

    public static int NO_OF_CONTROLS;
    public static String[] BUTTON_LIST;

    public static int SWITCH_TIME;

    public static String CURRENT_MENU;
    private Pin menuButton;

    public void destroy()
    {
        isAlive = false;
    }

    @Override
    public void run()
    {
        while (isAlive)
        {
            if (gameplay == null && musicBox == null)
            {
                selectEngine();
            }

            long timestamp = System.currentTimeMillis();
            if (lastDown > lastUp && lastDown < (timestamp - SWITCH_TIME))
            {
                if (gpio.getPinState(menuButton).isLow())
                {
                    System.out.println("Menu switch --> ");
                    SoundCache.MENU_SWITCH.play();

                    // Toggle Music / Hoespiel
                    if (CURRENT_MENU.equals(MUSICBOX))
                    {
                        if (!musicBox.isHoerspielSwitched())
                        {
                            // dont switch menu, just switch Hoerspiel/Music
                            // stop and start music to relaod folder
                            reset();
                            registerButton();

                            startMusicBox(true);
                            continue;
                        }
                    }

                    // toggle menu
                    toggleMenu();

                    reset();
                    registerButton();
                    continue;
                } else
                {
                    System.out.println("Somthing went wrong with the menu button... resetting menu timestamps");
                    resetButtonTimestamps();
                }
            }
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

        }

    }

    private void toggleMenu()
    {
        if (CURRENT_MENU.equals(GODFATHERSAYS))
        {
            CURRENT_MENU = MUSICBOX;
        } else
        {
            CURRENT_MENU = GODFATHERSAYS;
        }
    }

    private void selectEngine()
    {
        if (CURRENT_MENU.equals(MUSICBOX))
        {
            startMusicBox(false);
        } else
        {
            startGameEngine();
        }
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

    public void start()
    {
        System.out.println("Welcome ...");
        SoundCache.WELCOME.play();
        sleep(SoundCache.WELCOME.getMillisecondLength());

        reset();
        registerButton();

        // start Thread
        new Thread(this).start();
    }

    private void registerButton()
    {
        gpio = GPIOService.getInstance();

        if (NO_OF_CONTROLS > 0)
        {
            menuButton = RaspiPin.getPinByName(BUTTON_LIST[0]);

            gpio.addButtonListener(menuButton, new GpioPinListenerDigital()
            {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
                {
                    // Menu
                    if (event.getState().isLow())
                    {
                        // on button down
                        buttonTriggerDown();
                    }
                    if (event.getState().isHigh())
                    {
                        // on button up
                        buttonTriggerUp();
                    }
                }
            });
        }

    }

    public void buttonTriggerUp()
    {
        lastUp = System.currentTimeMillis();
    }

    public void buttonTriggerDown()
    {
        lastDown = System.currentTimeMillis();
    }

    private void reset()
    {
        if (gameplay != null)
        {
            gameplay.destroy();
            gameplay = null;
        }
        if (musicBox != null)
        {
            musicBox.destroy();
            musicBox = null;
        }
        resetButtonTimestamps();
    }

    private void resetButtonTimestamps()
    {
        lastDown = System.currentTimeMillis();
        lastUp = System.currentTimeMillis();
    }

    private void startGameEngine()
    {
        System.out.println("Game is starting ...");
        CURRENT_MENU = GODFATHERSAYS;
        gameplay = new GameEngine();
        gameplay.start();
    }

    private void startMusicBox(boolean hoerspielSwitched)
    {
        System.out.println("Musicbox is starting ...");
        CURRENT_MENU = MUSICBOX;
        musicBox = new MusicBox(hoerspielSwitched);
        musicBox.start();
    }

}
