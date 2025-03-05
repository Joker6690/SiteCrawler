import java.util.List;

public enum RegexConstants {

    ANCHOR_TAG("<a[\s]+([^>]+)>((?:.(?!</a>))*.)</a>"),
    HEADING_TAG("(<h[^>]+>(.*)</h[^>]+>|iU')"),
    REFERENCE_ATTRIBUTE("(href=[\"\'](.+?)[\"\'])"),
    TITLE_ATTRIBUTE("(title=[\"\'](.+?)[\"\'])"),
    ALTERNATE_CONTENT_ATTRIBUTE("(alt=[\"\'](.+?)[\"\'])");

    private final String value;

    RegexConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static List<RegexConstants> getRegexForExtractingLabels() {
        return List.of(HEADING_TAG, TITLE_ATTRIBUTE, ALTERNATE_CONTENT_ATTRIBUTE, ANCHOR_TAG);
    }
}
