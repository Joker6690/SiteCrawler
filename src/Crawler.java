import java.util.Set;
import java.util.concurrent.Callable;

public class Crawler implements Callable<Set<ContentDto>> {

    private ContentDto dto;
    private SiteCrawler siteCrawler;

    public Crawler(ContentDto dto, SiteCrawler siteCrawler) {
        this.dto = dto;
        this.siteCrawler = siteCrawler;
    }

    @Override
    public Set<ContentDto> call() {
        return siteCrawler.crawl(Set.of(dto));
    }

}

