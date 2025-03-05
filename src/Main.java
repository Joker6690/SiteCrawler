import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class Main {

    private static final Queue<ContentDto> RESULT = new LinkedBlockingQueue<>();
    private static final String STARTING_URL = "https://ecosio.com/en/";
    private static final int THREAD_COUNT = 20;
    private static final List<Future<Set<ContentDto>>> FUTURES = new ArrayList<>();

    public static void main(String[] args) {
        RESULT.add(new ContentDto("Home Page", STARTING_URL));
        System.out.println("Collecting links from " + STARTING_URL + " ...");
        try (ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT)) {
            firstSite();
            createTasks(service);
            getResults(service);
        } catch (IOException | ParserConfigurationException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        RESULT.stream().sorted(Comparator.comparing(ContentDto::getLabel)).distinct().forEach(System.out::println);
    }

    private static void getResults(ExecutorService service) throws InterruptedException, ExecutionException {
        for (var future : FUTURES) {
            RESULT.addAll(future.get().stream().filter(x -> !RESULT.contains(x)).collect(Collectors.toSet()));
            if (future.isDone() && RESULT.stream().allMatch(ContentDto::isDone)) {
                service.shutdown();
            }
        }
    }

    private static void createTasks(ExecutorService service) {
        for (var content : RESULT) {
            var siteCrawler = new SiteCrawler();
            var crawler = new Crawler(content, siteCrawler);
            FUTURES.add(service.submit(crawler));
        }
    }

    private static void firstSite() throws IOException, ParserConfigurationException {
        var siteCrawler = new SiteCrawler();
        var url = new URL(STARTING_URL);
        var links = siteCrawler.getAnchorList(url);
        RESULT.addAll(siteCrawler.handleAnchorTags(url, links));
    }
}