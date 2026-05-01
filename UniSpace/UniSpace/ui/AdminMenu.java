package UniSpace.ui;

import UniSpace.model.user.Admin;

import java.util.Scanner;

public class AdminMenu {

    private final Admin admin;
    private final Scanner scanner = new Scanner(System.in);

    public AdminMenu(Admin admin) {
        this.admin = admin;
    }

    public void show() {
        System.out.println("\n=== ADMIN MENU — " + admin.getFullName() + " ===");
        // TODO: реализует коллега
    }
}
