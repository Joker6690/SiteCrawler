import java.util.Set;
import java.util.concurrent.Callable;

public class Crawler implements Callable<Set<ContentDto>> {

    private ContentDto dto;
    private SiteCrawler crawler;

    public Crawler(ContentDto dto, SiteCrawler crawler) {
        this.dto = dto;
        this.crawler = crawler;
    }

    @Override
    public Set<ContentDto> call() {
        return crawler.crawl(Set.of(dto));
    }

}

