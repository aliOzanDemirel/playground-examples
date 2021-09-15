package clothing.service.repository;

import clothing.service.domain.Review;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends CrudRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.clothing.id = :clothingId")
    List<Review> findByClothingId(@Param("clothingId") Long clothingId);

}
