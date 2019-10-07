package org.th.godfatherSays;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadLocalRandom;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class MusicBox implements Runnable
{
    private GPIOService gpio;

    static int NO_OF_CONTROLS;
    static String[] BUTTON_LIST;
    static String[] LED_LIST;
    public static String[] MUSIC_FOLDER;
    public static String[] MUSIC_FOLDER_HOERSPIEL;
    static long BUTTON_FAILSAFE_MILLISECONDS;

    public static int BUTTON_INDEX_PLAY;
    public static int BUTTON_INDEX_BACK;
    public static int BUTTON_INDEX_NEXT;

    public static boolean SHUFFLE;

    public static String EASTER_EGG_FOLDER;

    public static boolean HOERSPIEL_STARTUP;

    private boolean hoerspielSwitched = false;

    private ArrayList<Pin> leds;
    private ArrayList<Pin> buttons;

    boolean alive = true;
    boolean pause = false;

    private Mp3 currentMp3;
    private LinkedBlockingDeque<File> mp3Files;

    private long failsaveTimestamp = 0;
    private int failsaveButton = 0;

    public MusicBox(boolean hoerspielSwitched)
    {
        System.out.println("Starting music box ...");

        this.hoerspielSwitched = hoerspielSwitched;

        checkForEasterEgg();

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
                    if (event.getState().isHigh())
                    {
                        // on button down
                        buttonTrigger(buttonIndex);
                        return;
                    }
                }
            });
        }
    }

    private void checkForEasterEgg()
    {
        // ~ every 100 starts the easter egg will be played
        int nextInt = ThreadLocalRandom.current().nextInt(1, 100);
        if (nextInt == 66)
        {
            try
            {
                mp3Files = new LinkedBlockingDeque<File>();
                listMp3(new File(EASTER_EGG_FOLDER));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void start()
    {
        new Thread(this).start();
    }

    @Override
    public void run()
    {
        while (alive)
        {
            if (!pause)
            {
                // reload after each song if list is empty
                checkForNewMp3Files();

                if (mp3Files != null && !mp3Files.isEmpty())
                {
                    if (currentMp3 != null)
                    {
                        currentMp3.stop();
                    }

                    String song = getSong().getAbsolutePath();
                    currentMp3 = new Mp3(song);
                    currentMp3.play();

                    if (!pause)
                    {
                        if (song.equals(getSong().getAbsolutePath()))
                        {
                            next();
                        }
                    }

                } else
                {
                    try
                    {
                        // If there was currently no song found wait 10 seconds
                        Thread.sleep(10000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            } else
            {
                // pause... pulse led
                gpio.ledToggle(leds.get(BUTTON_INDEX_PLAY - 1));

                try
                {
                    // If there was currently no song found wait 0.5 seconds
                    Thread.sleep(500);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        // gpio.shutdown();
    }

    private File getSong()
    {
        return mp3Files.peekFirst();
    }

    public void togglePlayStop()
    {
        System.out.println("MusicBox: toggle play/stop");
        if (pause)
        {
            play();
        } else
        {
            stop();
        }
    }

    public void play()
    {
        System.out.println("MusicBox: play");
        pause = false;
    }

    public void stop()
    {
        System.out.println("MusicBox: stop");
        pause = true;
        mp3Files = null;
        if (currentMp3 != null)
        {
            currentMp3.stop();
            currentMp3 = null;
        }
        checkForNewMp3Files();
    }

    public void next()
    {
        System.out.println("MusicBox: next");
        try
        {
            mp3Files.putLast(mp3Files.pollFirst());
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        if (currentMp3 != null)
        {
            currentMp3.stop();
            // currentMp3 = null;
        }
    }

    public void previous()
    {
        System.out.println("MusicBox: back");
        try
        {
            mp3Files.putFirst(mp3Files.pollLast());
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        if (currentMp3 != null)
        {
            currentMp3.stop();
            // currentMp3 = null;
        }
    }

    public void buttonTrigger(int button)
    {
        if (!alive)
        {
            System.out.println("Button [" + button + "] -> MusicBox is not alive... ignore!");
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

        gpio.ledOn(leds.get(button - 1), 200, false);

        if (button == BUTTON_INDEX_PLAY)
        {
            togglePlayStop();
        } else if (button == BUTTON_INDEX_NEXT)
        {
            next();
        } else if (button == BUTTON_INDEX_BACK)
        {
            previous();
        }
    }

    private void checkForNewMp3Files()
    {
        if (mp3Files == null || mp3Files.isEmpty())
        {
            loadMp3Files();
        } else if (mp3Files != null && !mp3Files.isEmpty() && !getSong().exists())
        {
            System.out.println("MP3 file not found... reload playlist");
            loadMp3Files();
        }
    }

    private void loadMp3Files()
    {
        mp3Files = new LinkedBlockingDeque<File>();

        for (String folder : getFolder())
        {
            try
            {
                listMp3(new File(folder));

                if (mp3Files != null && !mp3Files.isEmpty())
                {
                    // break after the first folder was found
                    break;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private String[] getFolder()
    {
        if (!hoerspielSwitched)
        {
            if (HOERSPIEL_STARTUP)
            {
                return MUSIC_FOLDER_HOERSPIEL;
            }
            return MUSIC_FOLDER;
        } else
        {
            if (HOERSPIEL_STARTUP)
            {
                return MUSIC_FOLDER;
            }
            return MUSIC_FOLDER_HOERSPIEL;
        }
    }

    public void destroy()
    {
        alive = false;

        if (currentMp3 != null)
        {
            currentMp3.stop();
        }
        gpio.shutdown();
    }

    private void listMp3(File fileFolder)
    {
        File[] filesArray = fileFolder.listFiles();
        List<File> files = Arrays.asList(filesArray);

        if (SHUFFLE)
        {
            // shuffle songs
            Collections.shuffle(files);
        }

        if (files != null)
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    // if this is a folder to the recursion
                    listMp3(file);
                } else
                {
                    String path = file.getPath();
                    if (path.length() > 5)
                    {
                        if (path.substring(path.length() - 4, path.length()).toLowerCase().equals(".mp3"))
                        {
                            // mp3 file found
                            mp3Files.addLast(file);
                            System.out.println("MP3 file found: " + file);
                        }
                    }
                }
            }
        }
    }

    public boolean isHoerspielSwitched()
    {
        return hoerspielSwitched;
    }

}
