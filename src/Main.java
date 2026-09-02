import java.util.Scanner;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
public class Main {
    private static String activeFilePath = null;
    private static XmlElement rootNode = null;

    private static final Map<String, Command> commands = new HashMap<>();
    public interface Command {
        void execute(String args);
    }
    public static class OpenCommand implements Command {
        @Override
        public void execute(String args) {
            if (args.isEmpty()) {
                System.out.println("Грешка: Моля, въведете път до файла.");
                return;
            }
            String path = args.replace("\"", "");
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
                System.out.println("Грешка при отваряне: " + e.getMessage());
            }
        }
    }

    public static class CloseCommand implements Command {
        @Override
        public void execute(String args) {
            if (activeFilePath != null) {
                System.out.println("Успешно затворен файл " + activeFilePath);
                activeFilePath = null;
                rootNode = null;
            } else {
                System.out.println("Грешка: В момента няма отворен файл.");
            }
        }
    }

    public static class SaveCommand implements Command {
        @Override
        public void execute(String args) {
            if (args.toLowerCase().startsWith("as ")) {
                String newPath = args.substring(3).trim().replace("\"", "");
                if (rootNode == null) {
                    System.out.println("Грешка: Няма отворен файл в паметта.");
                    return;
                }
                performSave(newPath);
                activeFilePath = newPath;
            } else if (args.isEmpty()) {
                if (rootNode == null || activeFilePath == null) {
                    System.out.println("Грешка: Няма отворен файл, който да бъде запазен.");
                    return;
                }
                performSave(activeFilePath);
            } else {
                System.out.println("Невалидна команда. Може би имахте предвид 'save as'?");
            }
        }
    }

    public static class PrintCommand implements Command {
        @Override
        public void execute(String args) {
            if (rootNode != null) printNode(rootNode, 0);
            else System.out.println("Грешка: Няма зареден файл в паметта.");
        }
    }

    public static class HelpCommand implements Command {
        @Override
        public void execute(String args) {
            System.out.println("Поддържани команди: open, close, save, save as, print, select, text, set, delete, children, child, newchild, xpath, help, exit");
        }
    }
    private static void initializeCommands() {
        commands.put("open", new OpenCommand());
        commands.put("close", new CloseCommand());
        commands.put("save", new SaveCommand());
        commands.put("print", new PrintCommand());
        commands.put("help", new HelpCommand());
        commands.put("select", new Command() {
            public void execute(String args) {
                String[] selArgs = args.split("\\s+");
                if (selArgs.length >= 2) selectAttribute(selArgs[0], selArgs[1]);
                else System.out.println("Грешка: Използвайте: select <id> <key>");
            }
        });

        commands.put("text", new Command() {
            public void execute(String args) {
                if (!args.isEmpty()) printNodeText(args);
                else System.out.println("Грешка: Използвайте: text <id>");
            }
        });

        commands.put("set", new Command() {
            public void execute(String args) {
                String[] setArgs = args.split("\\s+", 3);
                if (setArgs.length == 3) setAttribute(setArgs[0], setArgs[1], setArgs[2].replace("\"", ""));
                else System.out.println("Грешка: Използвайте: set <id> <key> <value>");
            }
        });

        commands.put("delete", new Command() {
            public void execute(String args) {
                String[] delArgs = args.split("\\s+");
                if (delArgs.length >= 2) deleteAttribute(delArgs[0], delArgs[1]);
                else System.out.println("Грешка: Използвайте: delete <id> <key>");
            }
        });

        commands.put("children", new Command() {
            public void execute(String args) {
                if (!args.isEmpty()) printChildrenAttributes(args);
                else System.out.println("Грешка: Използвайте: children <id>");
            }
        });

        commands.put("child", new Command() {
            public void execute(String args) {
                String[] childArgs = args.split("\\s+");
                if (childArgs.length >= 2) {
                    try {
                        int index = Integer.parseInt(childArgs[1]);
                        printNthChild(childArgs[0], index);
                    } catch (NumberFormatException e) {
                        System.out.println("Грешка: <n> трябва да бъде цяло число.");
                    }
                } else System.out.println("Грешка: Използвайте: child <id> <n>");
            }
        });

        commands.put("newchild", new Command() {
            public void execute(String args) {
                if (!args.isEmpty()) addNewChild(args);
                else System.out.println("Грешка: Използвайте: newchild <id>");
            }
        });

        commands.put("xpath", new Command() {
            public void execute(String args) {
                String[] xpathArgs = args.split("\\s+", 2);
                if (xpathArgs.length == 2) executeXPath(xpathArgs[0], xpathArgs[1]);
                else System.out.println("Грешка: Използвайте: xpath <id> <заявка>");
            }
        });
    }

    public static void main(String[] args) {
        initializeCommands();
        Scanner sc = new Scanner(System.in);
        System.out.println("XML Парсерът е стартиран. Напишете 'help' за команди.");

        while (true) {
            System.out.print("> ");
            String inputLine = sc.nextLine().trim();
            if (inputLine.isEmpty()) continue;

            String[] words = inputLine.split("\\s+", 2);
            String cmdName = words[0].toLowerCase();
            String argsStr = words.length > 1 ? words[1].trim() : "";

            if (cmdName.equals("exit")) {
                System.out.println("Излизане от програмата...");
                sc.close();
                return;
            }
            Command command = commands.get(cmdName);
            if (command != null) {
                try {
                    command.execute(argsStr);
                } catch (Exception e) {
                    System.out.println("Възникна грешка при изпълнение на командата: " + e.getMessage());
                }
            } else {
                System.out.println("Невалидна команда. Напишете 'help'.");
            }
        }
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

    private static void printChildrenAttributes(String id) {
        if (rootNode == null) { System.out.println("Грешка: Няма зареден файл."); return; }
        XmlElement target = findNodeById(rootNode, id);
        if (target != null) {
            if (target.getChildren().isEmpty()) { System.out.println("Елементът с ID '" + id + "' няма вложени наследници."); return; }
            System.out.println("Атрибути на наследниците на елемент '" + id + "':");
            for (XmlElement child : target.getChildren()) {
                System.out.print("Дете <" + child.getTagName() + "> (ID: " + child.getId() + ") -> ");
                if (child.getAttributes().isEmpty()) System.out.println("Няма допълнителни атрибути.");
                else {
                    for (Map.Entry<String, String> entry : child.getAttributes().entrySet()) { System.out.print(entry.getKey() + "=\"" + entry.getValue() + "\" "); }
                    System.out.println();
                }
            }
        } else System.out.println("Грешка: Елемент с ID '" + id + "' не съществува.");
    }

    private static void printNthChild(String id, int n) {
        if (rootNode == null) { System.out.println("Грешка: Няма зареден файл."); return; }
        XmlElement target = findNodeById(rootNode, id);
        if (target != null) {
            int childrenCount = target.getChildren().size();
            if (n >= 0 && n < childrenCount) {
                System.out.println("Показване на дете " + n + " за елемент '" + id + "':");
                printNode(target.getChildren().get(n), 0);
            } else System.out.println("Грешка: Невалиден индекс.");
        } else System.out.println("Грешка: Елемент с ID '" + id + "' не съществува.");
    }

    private static void setAttribute(String id, String key, String value) {
        if (rootNode == null) { System.out.println("Грешка: Няма зареден файл."); return; }
        XmlElement target = findNodeById(rootNode, id);
        if (target != null) {
            if (key.equals("id")) System.out.println("Грешка: Промяната на 'id' е забранена.");
            else {
                target.getAttributes().put(key, value);
                System.out.println("Успех: Атрибутът беше зададен.");
            }
        } else System.out.println("Грешка: Елемент с ID '" + id + "' не съществува.");
    }

    private static void deleteAttribute(String id, String key) {
        if (rootNode == null) { System.out.println("Грешка: Няма зареден файл."); return; }
        XmlElement target = findNodeById(rootNode, id);
        if (target != null) {
            if (key.equals("id")) System.out.println("Грешка: 'id' не може да се трие.");
            else if (target.getAttributes().containsKey(key)) {
                target.getAttributes().remove(key);
                System.out.println("Успех: Атрибутът беше изтрит.");
            } else System.out.println("Грешка: Атрибутът не съществува.");
        } else System.out.println("Грешка: Елемент с ID '" + id + "' не съществува.");
    }

    private static void addNewChild(String id) {
        if (rootNode == null) { System.out.println("Грешка: Няма зареден файл."); return; }
        XmlElement parent = findNodeById(rootNode, id);
        if (parent != null) {
            XmlElement newChild = new XmlElement("newElement");
            String generatedId = "id_" + java.util.UUID.randomUUID().toString().substring(0, 6);
            newChild.setId(generatedId);
            parent.addChild(newChild);
            System.out.println("Успех: Добавен е нов празен наследник с ID '" + generatedId + "'.");
        } else System.out.println("Грешка: Елемент с ID '" + id + "' не съществува.");
    }

    private static void executeXPath(String id, String query) {
        if (rootNode == null) {
            System.out.println("Грешка: Няма зареден файл.");
            return;
        }

        XmlElement startNode = findNodeById(rootNode, id);
        if (startNode == null) {
            System.out.println("Грешка: Елемент с ID '" + id + "' не съществува.");
            return;
        }

        List<XmlElement> currentNodes = new ArrayList<>();
        currentNodes.add(startNode);
        String[] steps = query.split("/");

        List<String> stringResults = new ArrayList<>();
        boolean returnsStrings = false;

        for (String step : steps) {
            List<XmlElement> nextNodes = new ArrayList<>();

            if (step.startsWith("(@") && step.endsWith(")")) {
                String attrName = step.substring(2, step.length() - 1);
                for (XmlElement el : currentNodes) {
                    if (attrName.equals("id")) {
                        stringResults.add(el.getId());
                    } else if (el.getAttributes().containsKey(attrName)) {
                        stringResults.add(el.getAttributes().get(attrName));
                    }
                }
                returnsStrings = true;
                break;
            }

            int targetIndex = -1;
            if (step.contains("[") && step.endsWith("]")) {
                int bracketPos = step.indexOf("[");
                try {
                    targetIndex = Integer.parseInt(step.substring(bracketPos + 1, step.length() - 1));
                } catch (Exception ignored) {}
                step = step.substring(0, bracketPos);
            }

            String filterChildName = null;
            String filterValue = null;
            if (step.contains("(") && step.endsWith(")")) {
                int parenPos = step.indexOf("(");
                String condition = step.substring(parenPos + 1, step.length() - 1);
                String[] parts = condition.split("=");
                if (parts.length == 2) {
                    filterChildName = parts[0];
                    filterValue = parts[1].replace("\"", "");
                }
                step = step.substring(0, parenPos);
            }

            String targetTagName = step;

            for (XmlElement el : currentNodes) {
                List<XmlElement> matchedChildren = new ArrayList<>();
                for (XmlElement child : el.getChildren()) {
                    if (child.getTagName().equals(targetTagName)) {
                        if (filterChildName != null) {
                            boolean passedFilter = false;
                            for (XmlElement grandChild : child.getChildren()) {
                                if (grandChild.getTagName().equals(filterChildName) &&
                                        grandChild.getTextContent().equals(filterValue)) {
                                    passedFilter = true;
                                    break;
                                }
                            }
                            if (passedFilter) {
                                matchedChildren.add(child);
                            }
                        } else {
                            matchedChildren.add(child);
                        }
                    }
                }

                if (targetIndex != -1) {
                    if (targetIndex >= 0 && targetIndex < matchedChildren.size()) {
                        nextNodes.add(matchedChildren.get(targetIndex));
                    }
                } else {
                    nextNodes.addAll(matchedChildren);
                }
            }
            currentNodes = nextNodes;
        }

        System.out.println("Резултати от XPath заявката:");
        if (returnsStrings) {
            for (String s : stringResults) { System.out.println("- " + s); }
            if (stringResults.isEmpty()) System.out.println("Не са намерени съвпадащи атрибути.");
        } else {
            for (XmlElement el : currentNodes) { printNode(el, 0); }
            if (currentNodes.isEmpty()) System.out.println("Не са намерени съвпадащи елементи.");
        }
    }
}