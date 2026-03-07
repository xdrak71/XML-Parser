import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("XML Parser started. Type 'help' for commands.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "help":
                    printHelp();
                    break;
                case "exit":
                    System.out.println("Exiting the program...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Unknown command. Type 'help'.");
            }
        }
    }

    private static void printHelp() {
        System.out.println("The following commands are supported:");
        System.out.println("help             prints this information");
        System.out.println("exit             exits the program");
    }
}