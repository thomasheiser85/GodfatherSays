package org.th.godfatherSays;

/**
 * GodfatherSays
 * 
 * @author Thomas Heiser
 * @version 1.0
 */
public class GodfatherSays
{

  public static void main(String[] args) throws InterruptedException
  {
    System.out.println("Starting ...");

    String configPath = null;
    if (args.length > 0)
    {
      configPath = args[0];
    }
    Config.loadConfig(configPath);

    Menu menu = new Menu();
    menu.start();

    // endless loop
    while (true)
    {
      Thread.sleep(10000);
    }

  }
}
