package example.armeria.rest.greeting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Greeting {
    private final int id;
    private final String message;

    @JsonCreator
    public Greeting(@JsonProperty("id") int id, @JsonProperty("message") String message) {
        this.id = id;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
