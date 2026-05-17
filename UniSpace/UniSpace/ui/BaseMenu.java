package UniSpace.ui;

import UniSpace.model.user.User;
import UniSpace.service.MessageService;
import UniSpace.storage.DataRepository;
import UniSpace.util.Colors;
import UniSpace.util.ConsoleHelper;

import java.util.List;
import java.util.Scanner;

/**
 * Abstract base for role menus that share inbox and messaging logic.
 * Pattern: Template Method — subclasses provide currentUser(), base handles viewInbox/sendMessage.
 */
abstract class BaseMenu {

    protected final MessageService messageService;
    protected final Scanner        scanner;

    protected BaseMenu(MessageService messageService) {
        this.messageService = messageService;
        this.scanner        = new Scanner(System.in);
    }

    protected abstract User currentUser();

    protected void viewInbox() {
        List<String> messages = messageService.getInbox(currentUser().getId());
        if (messages.isEmpty()) {
            System.out.println("  Inbox is empty.");
        } else {
            System.out.println(Colors.purple("\n  -- Inbox --")
                    + Colors.gray(" (" + messages.size() + " messages)"));
            messages.forEach(m -> System.out.println("  " + m));
            messageService.markAllRead(currentUser().getId());
            DataRepository.getInstance().save();
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    protected void sendMessage() {
        User recipient = ConsoleHelper.selectUserFromDirectory(
                DataRepository.getInstance().getUsers().values(),
                currentUser().getId(), null, scanner);
        if (recipient == null) return;
        System.out.print("  Message: ");
        String text = ConsoleHelper.readChoice(scanner);
        if (text.isEmpty()) {
            System.out.println(Colors.red("  [ERROR] Message cannot be empty."));
            return;
        }
        boolean sent = messageService.send(
                currentUser().getId(), currentUser().getFullName(),
                recipient.getId(), text);
        if (sent) {
            DataRepository.getInstance().save();
            System.out.println(Colors.green("  Message sent to " + recipient.getFullName() + "."));
        } else {
            System.out.println(Colors.red("  [ERROR] Could not send message."));
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }
}
