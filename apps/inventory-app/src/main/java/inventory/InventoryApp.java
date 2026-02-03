/*
 * This Java source file is a simple Java application.
 */
package inventory;

import profile.Profile;

public class InventoryApp {
    public String getGreeting() {
        String profile = new Profile().getCurrentProfile();
        return "Hi, " + profile + ".";
    }

    public static void main(String[] args) {
        System.out.println("[inventory-app] " + new InventoryApp().getGreeting());
    }
}

