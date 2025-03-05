import java.util.Objects;

public class ContentDto implements Comparable<ContentDto> {

    private final String label;
    private final String link;
    private boolean done;

    public ContentDto(String label, String link) {
        this.label = label;
        this.link = link;
        this.done = false;
    }

    public String getLabel() {
        return label;
    }

    public String getLink() {
        return link;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public int compareTo(ContentDto o) {
        return label.compareTo(o.label);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContentDto that)) return false;
        return this.link.equals(that.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }

    @Override
    public String toString() {
        return "ContentDto{" +
                "label='" + label + '\'' +
                ", link='" + link + '\'' +
                ", done=" + done +
                '}';
    }
}
