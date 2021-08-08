package app.repository;

public interface SequenceRepositoryBase {

    long getNextSequenceId(String key);
}
