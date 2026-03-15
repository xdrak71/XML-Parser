import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlElement {
    private String tagName;
    private String id;
    private Map<String, String> attributes;
    private List<XmlElement> children;
    private String textContent;

    public XmlElement(String tagName) {
        this.tagName = tagName;
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
        this.textContent = "";
    }

    public String getTagName() {
        return tagName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void addAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    public List<XmlElement> getChildren() {
        return children;
    }

    public void addChild(XmlElement child) {
        this.children.add(child);
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
}