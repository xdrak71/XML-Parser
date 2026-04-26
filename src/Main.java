import java.util.Scanner;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

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
                    if (!argsStr.isEmpty()) openFile(argsStr.replace("\"", ""));
                    else System.out.println("Грешка: Моля, въведете път до файла.");
                    break;
                case "close":
                    closeFile();
                    break;
                case "save":
                    if (argsStr.toLowerCase().startsWith("as ")) {
                        String newPath = argsStr.substring(3).trim().replace("\"", "");
                        saveFileAs(newPath);
                    } else if (argsStr.isEmpty()) {
                        saveFile();
                    } else {
                        System.out.println("Невалидна команда. Може би имахте предвид 'save as'?");
                    }
                    break;
                case "print":
                    if (rootNode != null) printNode(rootNode, 0);
                    else System.out.println("Грешка: Няма зареден файл в паметта.");
                    break;
                case "select":
                    String[] selArgs = argsStr.split("\\s+");
                    if (selArgs.length == 2) selectAttribute(selArgs[0], selArgs[1]);
                    else System.out.println("Грешка: Невалиден формат. Използвайте: select <id> <key>");
                    break;
                case "text":
                    if (!argsStr.isEmpty()) printNodeText(argsStr);
                    else System.out.println("Грешка: Невалиден формат. Използвайте: text <id>");
                    break;
                case "set":
                    String[] setArgs = argsStr.split("\\s+", 3);
                    if (setArgs.length == 3) {
                        setAttribute(setArgs[0], setArgs[1], setArgs[2].replace("\"", ""));
                    } else {
                        System.out.println("Грешка: Невалиден формат. Използвайте: set <id> <key> <value>");
                    }
                    break;
                case "delete":
                    String[] delArgs = argsStr.split("\\s+");
                    if (delArgs.length == 2) {
                        deleteAttribute(delArgs[0], delArgs[1]);
                    } else {
                        System.out.println("Грешка: Невалиден формат. Използвайте: delete <id> <key>");
                    }
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
                CustomXmlParser parser = new CustomXmlParser();
                rootNode = parser.parseFile(path);
                activeFilePath = path;
                System.out.println("Успешно отворен и прочетен файл " + path);
            } else {
                activeFilePath = path;
                rootNode = new XmlElement("root");
                rootNode.setId("0");
                System.out.println("Файлът не беше намерен. Създаден е нов празен документ в паметта.");
            }
        } catch (Exception e) {
            System.out.println("Възникна грешка при отварянето или четенето на файла: " + e.getMessage());
        }
    }

    private static void closeFile() {
        if (activeFilePath != null) {
            System.out.println("Успешно затворен файл " + activeFilePath);
            activeFilePath = null;
            rootNode = null;
        } else {
            System.out.println("Грешка: В момента няма отворен файл, който да бъде затворен.");
        }
    }

    private static void saveFile() {
        if (rootNode == null || activeFilePath == null) {
            System.out.println("Грешка: Няма отворен файл, който да бъде запазен.");
            return;
        }
        performSave(activeFilePath);
    }

    private static void saveFileAs(String newPath) {
        if (rootNode == null) {
            System.out.println("Грешка: Няма отворен файл в паметта.");
            return;
        }
        performSave(newPath);
        activeFilePath = newPath;
    }

    private static void performSave(String path) {
        try (PrintWriter writer = new PrintWriter(path)) {
            writeNodeToFile(writer, rootNode, 0);
            System.out.println("Файлът беше успешно запазен в: " + path);
        } catch (Exception e) {
            System.out.println("Грешка при записването на файла: " + e.getMessage());
        }
    }

    private static void writeNodeToFile(PrintWriter writer, XmlElement node, int depth) {
        if (node == null) return;
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < depth; i++) { spaces.append("  "); }
        writer.print(spaces.toString() + "<" + node.getTagName());
        if (node.getId() != null && !node.getId().isEmpty()) { writer.print(" id=\"" + node.getId() + "\""); }
        for (Map.Entry<String, String> attr : node.getAttributes().entrySet()) {
            writer.print(" " + attr.getKey() + "=\"" + attr.getValue() + "\"");
        }
        boolean hasChildren = !node.getChildren().isEmpty();
        boolean hasText = node.getTextContent() != null && !node.getTextContent().isEmpty();
        if (!hasChildren && !hasText) { writer.println(" />"); }
        else {
            writer.println(">");
            if (hasText) { writer.println(spaces.toString() + "  " + node.getTextContent()); }
            for (XmlElement child : node.getChildren()) { writeNodeToFile(writer, child, depth + 1); }
            writer.println(spaces.toString() + "</" + node.getTagName() + ">");
        }
    }
    private static void printNode(XmlElement node, int depth) {
        if (node == null) return;
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < depth; i++) { spaces.append("  "); }
        System.out.print(spaces.toString() + "<" + node.getTagName());
        if (node.getId() != null && !node.getId().isEmpty()) { System.out.print(" id=\"" + node.getId() + "\""); }
        for (Map.Entry<String, String> attr : node.getAttributes().entrySet()) {
            System.out.print(" " + attr.getKey() + "=\"" + attr.getValue() + "\"");
        }
        boolean hasChildren = !node.getChildren().isEmpty();
        boolean hasText = node.getTextContent() != null && !node.getTextContent().isEmpty();
        if (!hasChildren && !hasText) { System.out.println(" />"); }
        else {
            System.out.println(">");
            if (hasText) { System.out.println(spaces.toString() + "  " + node.getTextContent()); }
            for (XmlElement child : node.getChildren()) { printNode(child, depth + 1); }
            System.out.println(spaces.toString() + "</" + node.getTagName() + ">");
        }
    }

    private static XmlElement findNodeById(XmlElement currentNode, String targetId) {
        if (currentNode == null) return null;
        if (targetId.equals(currentNode.getId())) return currentNode;
        for (XmlElement child : currentNode.getChildren()) {
            XmlElement found = findNodeById(child, targetId);
            if (found != null) return found;
        }
        return null;
    }

    private static void selectAttribute(String id, String key) {
        if (rootNode == null) { System.out.println("Грешка: Няма зареден файл."); return; }
        XmlElement target = findNodeById(rootNode, id);
        if (target != null) {
            if (key.equals("id")) System.out.println(target.getId());
            else if (target.getAttributes().containsKey(key)) System.out.println(target.getAttributes().get(key));
            else System.out.println("Атрибутът '" + key + "' не беше намерен в този елемент.");
        } else System.out.println("Грешка: Елемент с ID '" + id + "' не съществува.");
    }

    private static void printNodeText(String id) {
        if (rootNode == null) { System.out.println("Грешка: Няма зареден файл."); return; }
        XmlElement target = findNodeById(rootNode, id);
        if (target != null) {
            String txt = target.getTextContent();
            if (txt != null && !txt.isEmpty()) System.out.println(txt);
            else System.out.println("Елементът няма текстово съдържание.");
        } else System.out.println("Грешка: Елемент с ID '" + id + "' не съществува.");
    }

    private static void printHelp() {
        System.out.println("Поддържани команди:");
        System.out.println("open <file>      - отваря <file>");
        System.out.println("close            - затваря текущия файл");
        System.out.println("save             - записва промените");
        System.out.println("save as <file>   - записва промените в нов файл");
        System.out.println("print            - извежда на екрана информацията");
        System.out.println("select <id> <k>  - извежда стойност на атрибут по ключ");
        System.out.println("text <id>        - извежда текста на даден елемент");
        System.out.println("set <id> <k> <v> - задава стойност на атрибут");
        System.out.println("delete <id> <k>  - изтрива атрибут");
        System.out.println("help             - показва това меню");
        System.out.println("exit             - спира програмата");
    }
    private static void setAttribute(String id, String key, String value) {
        if (rootNode == null) {
            System.out.println("Грешка: Няма зареден файл.");
            return;
        }

        XmlElement target = findNodeById(rootNode, id);
        if (target != null) {
            if (key.equals("id")) {
                System.out.println("Грешка: Промяната на атрибута 'id' е забранена, за да не се наруши уникалността в структурата.");
            } else {
                target.getAttributes().put(key, value);
                System.out.println("Успех: Атрибутът '" + key + "' беше зададен на '" + value + "' за елемент с ID '" + id + "'.");
            }
        } else {
            System.out.println("Грешка: Елемент с ID '" + id + "' не съществува.");
        }
    }

    private static void deleteAttribute(String id, String key) {
        if (rootNode == null) {
            System.out.println("Грешка: Няма зареден файл.");
            return;
        }

        XmlElement target = findNodeById(rootNode, id);
        if (target != null) {
            if (key.equals("id")) {
                System.out.println("Грешка: Не можете да изтриете системния атрибут 'id'.");
            } else if (target.getAttributes().containsKey(key)) {
                target.getAttributes().remove(key);
                System.out.println("Успех: Атрибутът '" + key + "' беше изтрит от елемент с ID '" + id + "'.");
            } else {
                System.out.println("Грешка: Атрибутът '" + key + "' не съществува в този елемент.");
            }
        } else {
            System.out.println("Грешка: Елемент с ID '" + id + "' не съществува.");
        }
    }
}