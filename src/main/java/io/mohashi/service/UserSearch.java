package io.mohashi.service;

import java.util.Optional;

import org.immutables.value.Value;

@Value.Immutable
public abstract class UserSearch {
    public enum QueryType {
        NAME,
        EMAIL,
        ALL,
        NONE;
    }

    public abstract Optional<String> name();
    public abstract Optional<String> email();

    public QueryType getType() {
        if (name().isPresent() && email().isEmpty()) {
            return QueryType.NAME;
        }

        if (email().isPresent() && name().isEmpty()) {
            return QueryType.EMAIL;
        }

        if (name().isPresent() && email().isPresent()) {
            return QueryType.ALL;
        }
        
        return QueryType.NONE;
    }
}
