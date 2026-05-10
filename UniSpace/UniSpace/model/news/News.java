package UniSpace.model.news;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class News implements Serializable {

    private static final long serialVersionUID = 1L;

    private String    newsId;
    private String    title;
    private String    content;
    private LocalDate date;
    private String    authorId;
    private String    authorName;

    public News() {}

    public News(String title, String content, String authorId, String authorName) {
        this.newsId     = UUID.randomUUID().toString();
        this.title      = title;
        this.content    = content;
        this.date       = LocalDate.now();
        this.authorId   = authorId;
        this.authorName = authorName;
    }

    public String    getNewsId()     { return newsId; }
    public String    getTitle()      { return title; }
    public String    getContent()    { return content; }
    public LocalDate getDate()       { return date; }
    public String    getAuthorId()   { return authorId; }
    public String    getAuthorName() { return authorName; }

    public void setTitle(String title)     { this.title   = title; }
    public void setContent(String content) { this.content = content; }

    @Override
    public String toString() {
        return String.format("[%s] %s  —  by %s", date, title, authorName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof News)) return false;
        return Objects.equals(newsId, ((News) o).newsId);
    }

    @Override
    public int hashCode() { return Objects.hash(newsId); }
}
