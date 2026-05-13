import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класът XmlElement представлява един възел (tag) в XML дървото.
 * Съхранява името на тага, неговото уникално ID, атрибути, текстово съдържание
 * и списък от вложени елементи (деца).
 */
public class XmlElement {
    private String tagName;
    private String id;
    private Map<String, String> attributes;
    private List<XmlElement> children;
    private String textContent;

    /**
     * Конструктор за нов XML елемент.
     * Инициализира празни колекции за атрибути и деца.
     *
     * @param tagName Името на XML тага.
     */
    public XmlElement(String tagName) {
        this.tagName = tagName;
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
        this.textContent = "";
    }

    /**
     * Връща името на тага.
     * @return Името на тага.
     */
    public String getTagName() { return tagName; }

    /**
     * Връща уникалния идентификатор на елемента.
     * @return Уникалният идентификатор.
     */
    public String getId() { return id; }

    /**
     * Задава нов идентификатор на елемента.
     * @param id Новият идентификатор.
     */
    public void setId(String id) { this.id = id; }

    /**
     * Връща речник с атрибутите на елемента (ключ-стойност).
     * @return Map съдържащ атрибутите.
     */
    public Map<String, String> getAttributes() { return attributes; }

    /**
     * Добавя или променя атрибут на елемента.
     *
     * @param key   Име на атрибута.
     * @param value Стойност на атрибута.
     */
    public void addAttribute(String key, String value) { this.attributes.put(key, value); }

    /**
     * Връща списък с вложените наследници на този елемент.
     * @return Списък от деца (XmlElement).
     */
    public List<XmlElement> getChildren() { return children; }

    /**
     * Добавя нов наследник към този елемент.
     * @param child Обект от тип XmlElement.
     */
    public void addChild(XmlElement child) { this.children.add(child); }

    /**
     * Връща текстовото съдържание на елемента.
     * @return Текстът между таговете.
     */
    public String getTextContent() { return textContent; }

    /**
     * Задава ново текстово съдържание на елемента.
     * @param textContent Новият текст.
     */
    public void setTextContent(String textContent) { this.textContent = textContent; }
}