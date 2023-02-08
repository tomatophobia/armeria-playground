package example.armeria.grpc.blog;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class BlogService extends BlogServiceGrpc.BlogServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(BlogService.class);
    private final AtomicInteger idGenerator = new AtomicInteger();
    private final Map<Integer, BlogPost> blogPosts = new ConcurrentHashMap<>();

    @Override
    public void createBlogPost(CreateBlogPostRequest request, StreamObserver<BlogPost> responseObserver) {
        final int id = idGenerator.getAndIncrement();
        final Instant now = Instant.now();
        final BlogPost updated = BlogPost.newBuilder()
                .setId(id)
                .setTitle(request.getTitle())
                .setContent(request.getContent())
                .setModifiedAt(now.toEpochMilli())
                .setCreatedAt(now.toEpochMilli())
                .build();
        blogPosts.put(id, updated);
        logger.info("Created at {} - {}", updated.getId(), updated.getTitle());
        responseObserver.onNext(updated);
        responseObserver.onCompleted();
    }

    @Override
    public void getBlogPost(GetBlogPostRequest request, StreamObserver<BlogPost> responseObserver) {
        final BlogPost blogPost = blogPosts.get(request.getId());
        if (blogPost == null) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription("The blog post does not exist. ID: " + request.getId())
                            .asRuntimeException()
            );
        } else {
            responseObserver.onNext(blogPost);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listBlogPosts(ListBlogPostsRequest request, StreamObserver<ListBlogPostsResponse> responseObserver) {
        final Collection<BlogPost> blogPosts;
        if (request.getDescending()) {
            blogPosts = this.blogPosts.entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(Entry::getKey)))
                    .map(Entry::getValue).collect(Collectors.toList());
        } else {
            blogPosts = this.blogPosts.values();
        }
        responseObserver.onNext(ListBlogPostsResponse.newBuilder().addAllBlogs(blogPosts).build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateBlogPost(UpdateBlogPostRequest request, StreamObserver<BlogPost> responseObserver) {
        final BlogPost oldBlogPost = blogPosts.get(request.getId());
        if (oldBlogPost == null) {
            throw new BlogNotFoundException("The blog post does not exist. ID: " + request.getId());
        } else {
            final BlogPost newBlogPost = oldBlogPost.toBuilder()
                    .setTitle(request.getTitle())
                    .setContent(request.getContent())
                    .setModifiedAt(Instant.now().toEpochMilli())
                    .build();
            blogPosts.put(request.getId(), newBlogPost);
            responseObserver.onNext(newBlogPost);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteBlogPost(DeleteBlogPostRequest request, StreamObserver<Empty> responseObserver) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        final BlogPost removed = blogPosts.remove(request.getId());
        if (removed == null) {
            responseObserver.onError(new BlogNotFoundException("The blog post does not exist. ID: " + request.getId()));
        } else {
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }
}
