package UniSpace.service;

import UniSpace.model.news.News;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NewsService {

    private static NewsService instance;

    public static NewsService getInstance() {
        if (instance == null) instance = new NewsService();
        return instance;
    }

    private NewsService() {}

    private final List<News> newsList = new ArrayList<>();

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public News addNews(String title, String content, String authorId, String authorName) {
        News news = new News(title, content, authorId, authorName);
        newsList.add(news);
        return news;
    }

    public boolean removeNews(String newsId) {
        return newsList.removeIf(n -> n.getNewsId().equals(newsId));
    }

    /** Returns all news sorted by date descending (newest first). */
    public List<News> getAllNews() {
        return newsList.stream()
                .sorted(Comparator.comparing(News::getDate).reversed())
                .collect(Collectors.toList());
    }

    /** Raw unsorted list — used only for serialization. */
    public List<News> getAllNewsRaw() {
        return new ArrayList<>(newsList);
    }

    // ── Restore from disk ──────────────────────────────────────────────────────

    public void restoreNews(List<News> loaded) {
        newsList.clear();
        if (loaded != null) newsList.addAll(loaded);
    }
}
