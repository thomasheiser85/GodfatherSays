package org.th.godfatherSays;

import java.util.HashMap;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class GPIOService
{
    private GpioController gpio;
    private static final GPIOService service = new GPIOService();
    private HashMap<Pin, GpioPinDigitalOutput> leds = new HashMap<Pin, GpioPinDigitalOutput>();
    private HashMap<Pin, GpioPinDigitalInput> buttons = new HashMap<Pin, GpioPinDigitalInput>();

    public static GPIOService getInstance()
    {
        if (service.gpio == null)
        {
            // create gpio controller
            service.gpio = GpioFactory.getInstance();
        }
        return service;
    }

    public GpioPinDigitalInput addButtonListener(Pin pin, GpioPinListenerDigital listener)
    {
        GpioPinDigitalInput button = registerButton(pin);

        // create and register gpio pin listener
        button.addListener(listener);

        return button;
    }

    public PinState getPinState(Pin pin)
    {
        GpioPinDigitalInput button = registerButton(pin);

        return button.getState();
    }

    private GpioPinDigitalInput registerButton(Pin pin)
    {
        GpioPinDigitalInput button;
        button = buttons.get(pin);

        if (button == null)
        {
            button = gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_DOWN);

            // set shutdown state for this input pin
            button.setShutdownOptions(true);

            buttons.put(pin, button);
        }
        return button;
    }

    private GpioPinDigitalOutput getLed(Pin pin)
    {
        GpioPinDigitalOutput led = leds.get(pin);
        if (led == null)
        {
            led = gpio.provisionDigitalOutputPin(pin, pin.toString());

            // set shutdown state for this pin
            led.setShutdownOptions(true, PinState.LOW);

            leds.put(pin, led);
        }

        return led;
    }

    public void ledOn(Pin pin)
    {
        getLed(pin).high();
    }

    public void ledOn(Pin pin, int time, boolean wait)
    {
        getLed(pin).pulse(time, wait);
    }

    public void ledOff(Pin pin)
    {
        getLed(pin).low();
    }

    public void ledToggle(Pin pin)
    {
        getLed(pin).toggle();
    }

    public void shutdown()
    {
        gpio.removeAllListeners();
        gpio.removeAllTriggers();
        // gpio.shutdown();
        // gpio = GpioFactory.getInstance();
    }

}
