import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomXmlParser {

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
        Stack<XmlElement> stack = new Stack<>();
        XmlElement root = null;
        Pattern tagPattern = Pattern.compile("<(/?)(\\w+)([^>]*)>");
        Matcher matcher = tagPattern.matcher(xml);

        int lastTextEnd = 0;

        while (matcher.find()) {
            boolean isClosingTag = matcher.group(1).equals("/");
            String tagName = matcher.group(2);
            if (!stack.isEmpty() && matcher.start() > lastTextEnd) {
                String innerText = xml.substring(lastTextEnd, matcher.start()).trim();
                if (!innerText.isEmpty()) {
                    stack.peek().setTextContent(innerText);
                }
            }
            lastTextEnd = matcher.end();

            if (isClosingTag) {
                if (!stack.isEmpty()) {
                    stack.pop();
                }
            } else {
                XmlElement newElement = new XmlElement(tagName);
                if (stack.isEmpty()) {
                    root = newElement;
                } else {
                    stack.peek().addChild(newElement);
                }
                String attributesPart = matcher.group(3).trim();
                if (!attributesPart.endsWith("/")) {
                    stack.push(newElement);
                }
            }
        }

        return root;
    }
}