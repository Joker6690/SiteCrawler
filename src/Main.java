import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class Main {

    private static final Queue<ContentDto> result = new LinkedBlockingQueue<>();
    private static final String startingUrl = "https://ecosio.com/en/";
    private static final int THREAD_COUNT = 20;
    private static final List<Future<Set<ContentDto>>> futures = new ArrayList<>();

    public static void main(String[] args) {
        result.add(new ContentDto("Home Page", startingUrl));
        try (ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT)) {
            firstSite();
            createTasks(service);
            getResults(service);
        } catch (IOException | ParserConfigurationException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        result.stream().sorted(Comparator.comparing(ContentDto::getLabel)).distinct().forEach(System.out::println);
    }

    private static void getResults(ExecutorService service) throws InterruptedException, ExecutionException {
        for (var future : futures) {
            result.addAll(future.get().stream().filter(x -> !result.contains(x)).collect(Collectors.toSet()));
            if (future.isDone()) {
                if (result.stream().allMatch(ContentDto::isDone)) {
                    service.shutdown();
                    break;
                }
            }
        }
    }

    private static void createTasks(ExecutorService service) {
        for (var content : result) {
            var siteCrawler = new SiteCrawler();
            var crawler = new Crawler(content, siteCrawler);
            futures.add(service.submit(crawler));
        }
    }

    private static void firstSite() throws IOException, ParserConfigurationException {
        var siteCrawler = new SiteCrawler();
        var url = new URL(startingUrl);
        var links = siteCrawler.getAnchorList(url);
        result.addAll(siteCrawler.handleAnchorTags(url, links));
    }
}