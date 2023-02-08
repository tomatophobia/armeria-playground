package example.armeria.grpc.blog;

public class BlogNotFoundException extends IllegalStateException {
    BlogNotFoundException(String s) {
        super(s);
    }
}
