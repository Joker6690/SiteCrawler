import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SiteCrawler {

    private static final String HEADER_KEY = "User-Agent";
    private static final String HEADER_VALUE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36";

    private final Set<ContentDto> result = new HashSet<>();

    public Set<ContentDto> crawl(Set<ContentDto> dtos) {

        for (var dto : dtos) {
            try {
                var url = new URL(dto.getLink());
                var links = getAnchorList(url);
                dto.setDone(true);
                var difference = handleAnchorTags(url, links).stream().filter(x -> !result.contains(x)).collect(Collectors.toSet());
                result.addAll(difference);
                if (!difference.isEmpty()) {
                    crawl(difference);
                }
            } catch (IOException | ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public Set<ContentDto> handleAnchorTags(URL url, Set<String> links) throws ParserConfigurationException {
        Set<ContentDto> result = new HashSet<>();
        var docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        docBuilder.setErrorHandler(new CustomErrorHandler());
        for (var line : links.stream().filter(x -> x.contains("href=\"" + url)).toList()) {
            try {
                var doc = docBuilder.parse(new InputSource(new StringReader(line)));
                result.add(handleTags(doc));
            } catch (SAXException | IOException e) {
                result.add(handleProblematicTags(line));
            }
        }
        return result;
    }


    public Set<String> getAnchorList(URL url) throws IOException {
        Set<String> links = new HashSet<>();

        URLConnection connection = url.openConnection();
        connection.setRequestProperty(HEADER_KEY, HEADER_VALUE);
        connection.connect();

        try (InputStream in = connection.getInputStream()) {
            var htmlPage = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Matcher pageMatcher = createMatcher(RegexConstants.ANCHOR_TAG.getValue(), htmlPage);
            while (pageMatcher.find()) {
                var tmp = pageMatcher.group().strip();
                links.add(tmp);
            }
            return links;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ContentDto handleTags(Document doc) {
        String label;
        if (!doc.getDocumentElement().getAttribute("title").isEmpty()) {
            label = doc.getDocumentElement().getAttribute("title");
        } else {
            label = doc.getDocumentElement().getTextContent().trim();
        }
        var link = doc.getDocumentElement().getAttribute("href");
        return new ContentDto(label, link);
    }

    public ContentDto handleProblematicTags(String anchorTag) {
        String label = null;
        for (var tag : RegexConstants.getRegexForExtractingLabels()) {
            label = getValueByRegex(tag, anchorTag);
            if(label != null && !label.isBlank()) {
                break;
            }
        }
        var link = getValueByRegex(RegexConstants.REFERENCE_ATTRIBUTE, anchorTag);
        return new ContentDto(label, link);

    }

    public String getValueByRegex(RegexConstants regex, String anchorTag) {
        var matcher = createMatcher(regex.getValue(), anchorTag);
        String label = null;
        if (matcher.find()) {
            label = matcher.group(2);
        }
        return label;
    }

    public Matcher createMatcher(String regex, String tag) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(tag);
    }
}
