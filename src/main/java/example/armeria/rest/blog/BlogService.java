package example.armeria.rest.blog;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.annotation.Blocking;
import com.linecorp.armeria.server.annotation.Default;
import com.linecorp.armeria.server.annotation.Delete;
import com.linecorp.armeria.server.annotation.ExceptionHandler;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.linecorp.armeria.server.annotation.Put;
import com.linecorp.armeria.server.annotation.RequestConverter;
import com.linecorp.armeria.server.annotation.RequestObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlogService {
    private final Map<Integer, BlogPost> blogPosts = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(BlogService.class);

    @Post("/blogs")
    @RequestConverter(BlogPostRequestConverter.class)
    public HttpResponse createBlogPost(BlogPost blogPost) {
        logger.debug("createBlogPost");
        blogPosts.put(blogPost.getId(), blogPost);
        return HttpResponse.ofJson(blogPost);
    }

    @Get("/blogs")
    @ProducesJson
    public Iterable<BlogPost> getBlogPosts(@Param @Default("true") boolean descending) {
        logger.debug("getBlogPosts");
        if (descending) {
            return blogPosts.entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(Entry::getKey)))
                    .map(Entry::getValue).collect(Collectors.toList());
        }
        return blogPosts.values().stream().collect(Collectors.toList());
    }

    @Get("/blogs/:id")
    public HttpResponse getBlogPost(@Param int id) {
        logger.debug("getBlogPost " + id);
        BlogPost blogPost = blogPosts.get(id);
        return HttpResponse.ofJson(blogPost);
    }

    @Put("/blogs/:id")
    public HttpResponse updateBlogPost(@Param int id, @RequestObject BlogPost blogPost) {
        logger.debug("updateBlogPost " + id);
        BlogPost oldBlogPost = blogPosts.get(id);
        if (oldBlogPost == null) {
            return HttpResponse.of(HttpStatus.NOT_FOUND);
        }
        BlogPost newBlogPost = new BlogPost(id, blogPost.getTitle(), blogPost.getContent(), oldBlogPost.getCreatedAt(), blogPost.getCreatedAt());
        blogPosts.put(id, newBlogPost);
        return HttpResponse.ofJson(newBlogPost);
    }

    @Blocking
    @Delete("/blogs/:id")
    @ExceptionHandler(BadRequestExceptionHandler.class)
    public HttpResponse deleteBlogPost(@Param int id) {
        logger.debug("deleteBlogPost " + id);
//
//        final Instant start = Instant.now();
//        Instant finish = Instant.now();
//        long timeElapsed = Duration.between(start, finish).toMillis();
//        while (timeElapsed < 1000 * 10) {
//            for (int i = 0; i < 100000000; i++) {}
//            finish = Instant.now();
//            timeElapsed = Duration.between(start, finish).toMillis();
//        }
//        logger.debug("deleteBlogPost end");
//
        BlogPost removed = blogPosts.remove(id);
        if (removed == null) {
            throw new IllegalArgumentException("The blog post does not exist. id: " + id);
        }
        return HttpResponse.of(HttpStatus.NO_CONTENT);
    }
}
