package example.armeria.grpc.blog.client;

import com.linecorp.armeria.client.grpc.GrpcClients;
import com.linecorp.armeria.client.logging.LoggingClient;
import example.armeria.blog.grpc.*;
import example.armeria.grpc.blog.*;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class BlogClient {
    private static final Logger logger = LoggerFactory.getLogger(BlogClient.class);
    static BlogServiceGrpc.BlogServiceBlockingStub client;

    void createBlogPost(String title, String content) {
        final CreateBlogPostRequest request = CreateBlogPostRequest.newBuilder()
                .setTitle(title)
                .setContent(content)
                .build();
        final BlogPost response = client.createBlogPost(request);
        logger.info("[Create response] Title: {} Content: {}", response.getTitle(), response.getContent());
    }

    static void getBlogPost(int id) {
        final BlogPost blogPost = client.getBlogPost(GetBlogPostRequest.newBuilder().setId(id).build());
    }

    static void listBlogPosts() {
        final ListBlogPostsResponse response = client.listBlogPosts(ListBlogPostsRequest.newBuilder().setDescending(false).build());
        final List<BlogPost> blogs = response.getBlogsList();
    }

    static void updateBlogPost(Integer id, String newTitle, String newContent) {
        final UpdateBlogPostRequest request = UpdateBlogPostRequest.newBuilder()
                .setId(id)
                .setTitle(newTitle)
                .setContent(newContent)
                .build();
        final BlogPost updated = client.updateBlogPost(request);
    }

    static void deleteBlogPost(int id) {
        final DeleteBlogPostRequest request = DeleteBlogPostRequest.newBuilder().setId(id).build();
        try {
            client.deleteBlogPost(request);
        } catch (StatusRuntimeException statusRuntimeException) {

        }
    }

    void testRun(){
        createBlogPost("Another blog post", "Creating a post via createBlogPost().");
        deleteBlogPost(0);
        listBlogPosts();
//        getBlogPost(1);
//        updateBlogPost(10000, "New title", "New content.");
//        getBlogPost(1);
    }

    public static void main(String[] args) throws Exception {
//        client = GrpcClients.newClient("http://127.0.0.1:8080/", BlogServiceGrpc.BlogServiceBlockingStub.class);
        client = GrpcClients.builder("http://127.0.0.1:8080/")
                .decorator(LoggingClient.newDecorator())
                .build(BlogServiceGrpc.BlogServiceBlockingStub.class);
        CreateBlogPostRequest request = CreateBlogPostRequest.newBuilder()
                .setTitle("My first blog")
                .setContent("Yay")
                .build();
        BlogPost response = client.createBlogPost(request);

        BlogClient blogClient = new BlogClient();
        blogClient.testRun();
    }
}
