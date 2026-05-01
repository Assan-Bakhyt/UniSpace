package UniSpace.ui;

import UniSpace.model.user.Manager;

import java.util.Scanner;

public class ManagerMenu {

    private final Manager manager;
    private final Scanner scanner = new Scanner(System.in);

    public ManagerMenu(Manager manager) {
        this.manager = manager;
    }

    public void show() {
        System.out.println("\n=== MANAGER MENU — " + manager.getFullName() + " ===");
        // TODO: реализует коллега
    }
}
