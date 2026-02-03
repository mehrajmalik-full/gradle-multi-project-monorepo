/*
 * This Java source file is a simple Java application.
 */
package account;

import greeter.Greeter;
import profile.Profile;

public class AccountApp {
    public String getGreeting() {
        String profile = new Profile().getCurrentProfile();
        return "Hi, " + profile + ". " + new Greeter().getGreeting();
    }

    public static void main(String[] args) {
        System.out.println("[account-service]: " + new AccountApp().getGreeting());
    }

    public int sum(int a, int b) {
        return a + b;
    }

}

