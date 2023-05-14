package example.armeria.rest.blog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlogPost {
    private final int id;           // The post ID
    private final String title;     // The post title
    private final String content;   // The post content
    private final long createdAt;   // The time post is created at
    private final long modifiedAt;  // The time post is modified at

    @JsonCreator
    BlogPost(@JsonProperty("id") int id, @JsonProperty("title") String title,
             @JsonProperty("content") String content) {
        this(id, title, content, System.currentTimeMillis());
    }

    BlogPost(int id, String title, String content, long createdAt) {
        this(id, title, content, createdAt, createdAt);
    }

    BlogPost(int id, String title, String content, long createdAt, long modifiedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }
}
