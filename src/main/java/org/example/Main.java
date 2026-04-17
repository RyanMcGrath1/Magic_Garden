package org.example;

import org.example.browser.util.ItemsToBuy;
import org.example.scripts.MagicGardenAutomation;

public class Main {
    public static void main(String[] args) throws Exception {
/* 
    This is the main class that will be used to run the script.
    It will create an instance of MagicGardenAutomation and call the begin method.
    The begin method will take an array of ItemsToBuy enum values and pass them to MagicGardenAutomation.
    MagicGardenAutomation then runs the shop automation flow.
    The ItemsToBuy enum values are used to determine which items will actualy be bought.
    The purpose of this enum is to make it easy to add new items to the list of items to buy.
*/
    MagicGardenAutomation automation = MagicGardenAutomation.getInstance();
    automation.begin(ItemsToBuy.values());
    }
}
