package UniSpace.main;

import UniSpace.storage.DataRepository;
import UniSpace.ui.LoginMenu;

public class Main {
    public static void main(String[] args) {
        DataRepository.getInstance();
        new LoginMenu().show();
    }
}