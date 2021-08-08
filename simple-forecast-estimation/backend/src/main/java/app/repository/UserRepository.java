package app.repository;


import app.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>, UserRepositoryBase {

    void deleteByUsername(String username);

    Optional<User> findByUsername(String username);

    @Override
    <S extends User> S insert(S s);
}
