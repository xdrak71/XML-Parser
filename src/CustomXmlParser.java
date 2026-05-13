import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CustomXmlParser е отговорен за синтактичния анализ на XML файлове.
 * Използва стек (Stack) и регулярни изрази (Regex), за да изгради дървовидна структура
 * от обекти в паметта без външни библиотеки.
 */
public class CustomXmlParser {

    private Set<String> usedIds = new HashSet<>();
    private int generatedIdCounter = 1;

    /**
     * Конструктор по подразбиране за инициализиране на парсера.
     */
    public CustomXmlParser() {
    }

    /**
     * Прочита XML файл от диска и го преобразува в дърво от XmlElement обекти.
     *
     * @param filePath Път до файла на диска.
     * @return Коренният елемент на XML дървото.
     * @throws FileNotFoundException Ако файлът не бъде намерен на диска.
     */
    public XmlElement parseFile(String filePath) throws FileNotFoundException {
        Scanner fileScanner = new Scanner(new File(filePath));
        StringBuilder xmlText = new StringBuilder();

        while (fileScanner.hasNextLine()) {
            xmlText.append(fileScanner.nextLine().trim());
        }
        fileScanner.close();

        return buildTreeFromString(xmlText.toString());
    }

    private XmlElement buildTreeFromString(String xml) {
        usedIds.clear();
        generatedIdCounter = 1;

        Stack<XmlElement> stack = new Stack<>();
        XmlElement root = null;

        Pattern tagPattern = Pattern.compile("<(/?)(\\w+)([^>]*)>");
        Matcher matcher = tagPattern.matcher(xml);

        int lastTextEnd = 0;

        while (matcher.find()) {
            boolean isClosingTag = matcher.group(1).equals("/");
            String tagName = matcher.group(2);
            String attributesPart = matcher.group(3).trim();

            if (!stack.isEmpty() && matcher.start() > lastTextEnd) {
                String innerText = xml.substring(lastTextEnd, matcher.start()).trim();
                if (!innerText.isEmpty()) {
                    stack.peek().setTextContent(innerText);
                }
            }
            lastTextEnd = matcher.end();

            if (isClosingTag) {
                if (!stack.isEmpty()) stack.pop();
            } else {
                XmlElement newElement = new XmlElement(tagName);
                parseAttributes(newElement, attributesPart);
                newElement.setId(ensureUniqueId(newElement.getId()));

                if (stack.isEmpty()) {
                    root = newElement;
                } else {
                    stack.peek().addChild(newElement);
                }

                if (!attributesPart.endsWith("/")) {
                    stack.push(newElement);
                }
            }
        }
        return root;
    }

    private void parseAttributes(XmlElement element, String attributesStr) {
        Pattern attrPattern = Pattern.compile("(\\w+)\\s*=\\s*\"([^\"]*)\"");
        Matcher attrMatcher = attrPattern.matcher(attributesStr);
        while (attrMatcher.find()) {
            String key = attrMatcher.group(1);
            String value = attrMatcher.group(2);
            if (key.equals("id")) element.setId(value);
            else element.addAttribute(key, value);
        }
    }

    private String ensureUniqueId(String originalId) {
        if (originalId == null || originalId.isEmpty()) {
            String newId = "gen_id_" + generatedIdCounter++;
            while (usedIds.contains(newId)) newId = "gen_id_" + generatedIdCounter++;
            usedIds.add(newId);
            return newId;
        }
        if (!usedIds.contains(originalId)) {
            usedIds.add(originalId);
            return originalId;
        }
        int suffix = 1;
        String newId = originalId + "_" + suffix;
        while (usedIds.contains(newId)) {
            suffix++;
            newId = originalId + "_" + suffix;
        }
        usedIds.add(newId);
        return newId;
    }
}