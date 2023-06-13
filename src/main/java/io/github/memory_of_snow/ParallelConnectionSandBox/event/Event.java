package io.github.memory_of_snow.ParallelConnectionSandBox.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Event {

    private final String id;
    private final int kind;

    private final String pubkey;

    private final long created_at;
    private final String content;

    private final List<List<String>> tags;
    private final String sig;


    public static class Builder {
        private String id;
        private int kind;

        private String pubkey;

        private long created_at;
        private String content;

        private List<List<String>> tags = new ArrayList<>();
        private String sig;

        public Builder() {

        }

        public Builder id(String value) {
            id = value;
            return this;
        }

        public Builder kind(int value) {
            kind = value;
            return this;
        }

        public Builder pubkey(String value) {
            pubkey = value;
            return this;
        }

        public Builder created_at(long value) {
            created_at = value;
            return this;
        }

        public Builder content(String value) {
            content = value;
            return this;
        }

        public Builder tags(List<List<String>> value) {
            tags = value;
            return this;
        }

        public Builder sig(String value) {
            sig = value;
            return this;
        }

        public Event build() {
            return new Event(this);
        }
    }

    private Event(Builder builder) {
        id = builder.id;
        kind = builder.kind;
        pubkey = builder.pubkey;
        created_at = builder.created_at;
        content = builder.content;
        tags = builder.tags;
        sig = builder.sig;
    }

    public String getId() {
        return id;
    }

    public int getKind() {
        return kind;
    }

    public String getPubkey() {
        return pubkey;
    }

    public long getCreated_at() {
        return created_at;
    }

    public String getContent() {
        return content;
    }

    public List<List<String>> getTags() {
        return tags;
    }

    public String getSig() {
        return sig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}




