package app.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Sequence {

    @Id
    private String id;

    private long seq;

    public Sequence() {
    }

    public Sequence(String id, long seq) {
        this.id = id;
        this.seq = seq;
    }

    public String getId() {
        return id;
    }

    public long getSeq() {
        return seq;
    }
}