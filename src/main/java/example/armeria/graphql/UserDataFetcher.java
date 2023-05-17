package example.armeria.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.Map;

public class UserDataFetcher implements DataFetcher<User> {

    private final Map<String, User> data = Map.of("1", new User("1", "hero"),
            "2", new User("2", "human"),
            "3", new User("3", "droid"));

    @Override
    public User get(DataFetchingEnvironment environment) throws Exception {
        final String id = environment.getArgument("id");
        return data.get(id);
    }

}
