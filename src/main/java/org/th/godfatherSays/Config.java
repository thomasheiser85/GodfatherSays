package org.th.godfatherSays;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

public class Config
{

    private static final String TRIGGER_DOWN = "DOWN";
    private static final String SEPARATOR = ",";
    private static final String TRUE = "true";

    // static final String RESOURCES = "resources/";
    static final String WAV = /* RESOURCES + */ "wav/";
    private static final String DEFAULT_CONFIG = /* RESOURCES + */ "config/default_config.properties";

    private static final String CONFIG_GAME_START_LEVEL = "game.start.level";
    private static final String CONFIG_GAME_START_LED_TIME = "game.start.led.time";
    private static final String CONFIG_GAME_LED_OFF_TIME_PERCENT = "game.led.off.time.percent";
    private static final String CONFIG_GAME_ROUND_FASTER_PERCENT = "game.round.faster.percent";
    private static final String CONFIG_GAME_BUTTON_TRIGGER = "game.button.trigger";
    private static final String CONFIG_GAME_BUTTON_FAILSAFE_MILLISECONDS = "game.button.failsafe.milliseconds";
    private static final String CONFIG_GAME_BUTTON_TRIGGER_LED_TIME = "game.button.trigger.led.time";
    private static final String CONFIG_GAME_BUTTON_SOUND = "game.button.sound";
    private static final String CONFIG_GAME_BUTTON_SOUNDS = "game.button.sounds";
    private static final String CONFIG_SOUND_WELCOME = "sound.welcome";
    private static final String CONFIG_SOUND_BUTTON_PRESS = "sound.button.press";
    private static final String CONFIG_SOUND_WIN = "sound.win";
    private static final String CONFIG_SOUND_FAIL = "sound.fail";
    private static final String CONFIG_SOUND_AWAITING_INPUT = "sound.awaiting.input";
    private static final String CONFIG_SOUND_MENU_SWITCH = "sound.menu.switch";
    private static final String CONFIG_HARDWARE_CONTROLS = "hardware.controls";
    private static final String CONFIG_HARDWARE_BUTTON_LIST = "hardware.button.list";
    private static final String CONFIG_HARDWARE_LED_LIST = "hardware.led.list";
    private static final String CONFIG_MUSIC_FOLDER = "music.folders";
    private static final String CONFIG_MUSIC_FOLDER_HOERSPIEL = "music.foldersHoerspiel";
    private static final String CONFIG_MUSIC_BUTTON_PLAY = "music.button.play";
    private static final String CONFIG_MUSIC_BUTTON_BACK = "music.button.back";
    private static final String CONFIG_MUSIC_BUTTON_NEXT = "music.button.next";
    private static final String CONFIG_MUSIC_VOLUME = "music.volume";
    private static final String CONFIG_MUSIC_SHUFFLE = "music.shuffle";
    private static final String CONFIG_MUSIC_HOERSPIEL_STARTUP = "music.hoerspielStartup";
    private static final String CONFIG_MENU_SWITCH_TIME = "menu.switch.time";
    private static final String CONFIG_MENU_START = "menu.start";

    private static final String EASTER_EGG_MUSIC_FOLDER = "easter.egg.music.folder";

    public static void loadConfig(String configPath)
    {
        System.out.println("Load properties ...");
        try
        {
            InputStream stream;
            Properties config = new Properties();

            if (configPath == null)
            {
                URL url = GodfatherSays.class.getClassLoader().getResource(DEFAULT_CONFIG);
                stream = url.openStream();
            } else
            {
                stream = new BufferedInputStream(new FileInputStream(configPath));
            }
            config.load(stream);
            stream.close();

            readConfig(config);

            System.out.println(config);
        } catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("Failed loading properties...");
        }
    }

    private static int toInt(String value)
    {
        return Integer.parseInt(value.trim());
    }

    private static void readConfig(Properties config)
    {
        System.out.println("Load sound cache ...");
        SoundCache.WELCOME = new Sound(WAV + config.getProperty(CONFIG_SOUND_WELCOME));
        SoundCache.BUTTON_PRESS = new Sound(WAV + config.getProperty(CONFIG_SOUND_BUTTON_PRESS));
        SoundCache.WIN = new Sound(WAV + config.getProperty(CONFIG_SOUND_WIN));
        SoundCache.FAIL = new Sound(WAV + config.getProperty(CONFIG_SOUND_FAIL));
        SoundCache.AWAITING_INPUT = new Sound(WAV + config.getProperty(CONFIG_SOUND_AWAITING_INPUT));
        SoundCache.MENU_SWITCH = new Sound(WAV + config.getProperty(CONFIG_SOUND_MENU_SWITCH));

        String buttonsSoundString = config.getProperty(CONFIG_GAME_BUTTON_SOUNDS);
        if (buttonsSoundString != null && !buttonsSoundString.isEmpty())
        {
            ArrayList<Sound> buttonSoundList = new ArrayList<>();

            String[] buttonsSounds = buttonsSoundString.split(SEPARATOR);
            for (String sound : buttonsSounds)
            {
                buttonSoundList.add(new Sound(WAV + sound));
            }
            SoundCache.BUTTON_SOUNDS = buttonSoundList;
        }

        System.out.println("Load menu settings ...");
        Menu.SWITCH_TIME = toInt(config.getProperty(CONFIG_MENU_SWITCH_TIME));
        Menu.CURRENT_MENU = config.getProperty(CONFIG_MENU_START);

        System.out.println("Load hardware settings ...");
        GameEngine.NO_OF_CONTROLS = toInt(config.getProperty(CONFIG_HARDWARE_CONTROLS));
        GameEngine.BUTTON_LIST = config.getProperty(CONFIG_HARDWARE_BUTTON_LIST).split(SEPARATOR);
        GameEngine.LED_LIST = config.getProperty(CONFIG_HARDWARE_LED_LIST).split(SEPARATOR);
        MusicBox.NO_OF_CONTROLS = toInt(config.getProperty(CONFIG_HARDWARE_CONTROLS));
        MusicBox.BUTTON_LIST = config.getProperty(CONFIG_HARDWARE_BUTTON_LIST).split(SEPARATOR);
        MusicBox.LED_LIST = config.getProperty(CONFIG_HARDWARE_LED_LIST).split(SEPARATOR);
        Menu.NO_OF_CONTROLS = toInt(config.getProperty(CONFIG_HARDWARE_CONTROLS));
        Menu.BUTTON_LIST = config.getProperty(CONFIG_HARDWARE_BUTTON_LIST).split(SEPARATOR);

        System.out.println("Load game settings ...");
        GameEngine.START_LEVEL = toInt(config.getProperty(CONFIG_GAME_START_LEVEL));
        GameEngine.START_LED_TIME = toInt(config.getProperty(CONFIG_GAME_START_LED_TIME));
        GameEngine.LED_OFF_PERCENT = toInt(config.getProperty(CONFIG_GAME_LED_OFF_TIME_PERCENT));
        GameEngine.ROUND_FASTER = toInt(config.getProperty(CONFIG_GAME_ROUND_FASTER_PERCENT));
        GameEngine.BUTTON_TRIGGER_UP = !config.getProperty(CONFIG_GAME_BUTTON_TRIGGER).equals(TRIGGER_DOWN);
        GameEngine.BUTTON_FAILSAFE_MILLISECONDS = toInt(config.getProperty(CONFIG_GAME_BUTTON_FAILSAFE_MILLISECONDS));
        GameEngine.BUTTON_TRIGGER_LED_TIME = toInt(config.getProperty(CONFIG_GAME_BUTTON_TRIGGER_LED_TIME));
        GameEngine.BUTTON_SOUND = config.getProperty(CONFIG_GAME_BUTTON_SOUND).equals(TRUE);

        System.out.println("Load music settings ...");
        MusicBox.MUSIC_FOLDER = config.getProperty(CONFIG_MUSIC_FOLDER).split(SEPARATOR);
        MusicBox.MUSIC_FOLDER_HOERSPIEL = config.getProperty(CONFIG_MUSIC_FOLDER_HOERSPIEL).split(SEPARATOR);
        MusicBox.BUTTON_FAILSAFE_MILLISECONDS = toInt(config.getProperty(CONFIG_GAME_BUTTON_FAILSAFE_MILLISECONDS));
        MusicBox.BUTTON_INDEX_PLAY = toInt(config.getProperty(CONFIG_MUSIC_BUTTON_PLAY));
        MusicBox.BUTTON_INDEX_BACK = toInt(config.getProperty(CONFIG_MUSIC_BUTTON_BACK));
        MusicBox.BUTTON_INDEX_NEXT = toInt(config.getProperty(CONFIG_MUSIC_BUTTON_NEXT));
        Mp3.VOLUME = config.getProperty(CONFIG_MUSIC_VOLUME);
        MusicBox.SHUFFLE = config.getProperty(CONFIG_MUSIC_SHUFFLE).equals(TRUE);
        MusicBox.HOERSPIEL_STARTUP = config.getProperty(CONFIG_MUSIC_HOERSPIEL_STARTUP).equals(TRUE);

        MusicBox.EASTER_EGG_FOLDER = config.getProperty(EASTER_EGG_MUSIC_FOLDER);

    }

}
