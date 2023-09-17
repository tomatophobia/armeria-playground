package example.armeria.rest.blog;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.*;
import com.linecorp.armeria.common.util.SafeCloseable;
import com.linecorp.armeria.server.annotation.*;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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

    @Get("/blogs/test")
    public HttpResponse getTest(RequestContext ctx) {
        logger.info("getTest");
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        WebClient client = WebClient.of("https://icanhazdadjoke.com");
        HttpResponse response = client.execute(RequestHeaders.of(HttpMethod.GET, "/", HttpHeaderNames.ACCEPT, HttpHeaderValues.APPLICATION_JSON));
//         1. 수동으로 컨텍스트 정보 전달
//        response.aggregate().thenAccept(aggregatedRes -> {
//            try (SafeCloseable ignored = ctx.push()) {
//                logger.info("another executor");
//                throw new IllegalStateException("test");
////                future.complete(aggregatedRes.toHttpResponse());
//            }
//        }).whenComplete((t, u) -> {
//            if (u != null) {
//                logger.info("예외 발생");
//            }
//        });
        // 2. ContextAwareFuture
        CompletableFuture<AggregatedHttpResponse> contextAwareFuture = ctx.makeContextAware(response.aggregate());
        CompletableFuture f = contextAwareFuture.thenAccept(res -> {
            logger.info("응답");
//            throw new IllegalStateException("예외");
            future.complete(res.toHttpResponse());
        });
        // 3. ContextAwareExecutor
//        Executor contextAwareExecutor = ctx.eventLoop();
//        response.aggregate().thenAcceptAsync(res -> {
//            logger.info("응답");
//            throw new IllegalStateException("예외");
//        }, contextAwareExecutor);

        return HttpResponse.from(future);
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
        BlogPost removed = blogPosts.remove(id);
        if (removed == null) {
            throw new IllegalArgumentException("The blog post does not exist. id: " + id);
        }
        return HttpResponse.of(HttpStatus.NO_CONTENT);
    }
}
