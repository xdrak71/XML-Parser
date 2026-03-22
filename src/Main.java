import java.util.Scanner;

public class Main {
    private static String activeFilePath = null;
    private static XmlElement rootNode = null;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("XML Парсерът е стартиран. Напишете 'help' за списък с команди.");

        while (true) {
            System.out.print("> ");
            String inputLine = sc.nextLine().trim();

            if (inputLine.isEmpty()) {
                continue;
            }
            String[] words = inputLine.split("\\s+", 2);
            String cmd = words[0].toLowerCase();
            String argsStr = words.length > 1 ? words[1].trim() : "";

            switch (cmd) {
                case "open":
                    if (!argsStr.isEmpty()) {
                        openFile(argsStr.replace("\"", ""));
                    } else {
                        System.out.println("Грешка: Моля, въведете път до файла.");
                    }
                    break;
                case "close":
                    closeFile();
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                    System.out.println("Излизане от програмата...");
                    sc.close();
                    return;
                default:
                    System.out.println("Невалидна команда. Напишете 'help'.");
            }
        }
    }
    private static void openFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                activeFilePath = path;
                rootNode = new XmlElement("root");
                System.out.println("Успешно отворен файл " + path);}
            else{
                activeFilePath = path;
                rootNode = new XmlElement("root");
                rootNode.setId("0"); // Даваме му базово ID
                System.out.println("Файлът не беше намерен. Създаден е нов празен документ в паметта.");
            }}
        catch (Exception e){
            System.out.println("Възникна грешка при отварянето на файла: " + e.getMessage());
        }
    }

    private static void closeFile() {
        if(activeFilePath != null) {
            System.out.println("Успешно затворен файл "+activeFilePath);
            activeFilePath = null;
            rootNode = null;}
        else{
            System.out.println("Грешка: В момента няма отворен файл, който да бъде затворен.");
        }
    }

    private static void printHelp() {
        System.out.println("Поддържани команди:");
        System.out.println("open <file>      - отваря <file>");
        System.out.println("close            - затваря текущия файл");
        System.out.println("help             - показва това меню");
        System.out.println("exit             - спира програмата");
    }
}