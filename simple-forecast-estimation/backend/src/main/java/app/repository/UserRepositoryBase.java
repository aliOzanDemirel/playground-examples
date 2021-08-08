package app.repository;

import app.domain.HighScore;
import app.domain.User;

import java.util.List;

public interface UserRepositoryBase {

    boolean changePassword(String oldPassword, String newPassword, String username);

    List<HighScore> findAllByOrderByHighScoreDesc();

    <S extends User> S insert(S s);
}
