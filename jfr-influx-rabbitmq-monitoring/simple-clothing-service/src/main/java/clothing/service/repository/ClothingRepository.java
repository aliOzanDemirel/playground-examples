package clothing.service.repository;

import clothing.service.domain.Clothing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ClothingRepository extends CrudRepository<Clothing, Long> {

    // can be optimized by investigating generated native query or by creating indexes for commonly used fields
    @Query("SELECT DISTINCT c FROM Clothing c" +
            " INNER JOIN c.clothingSizes si" +
            " INNER JOIN c.clothingColors cc" +
            " INNER JOIN cc.color col" +
            " WHERE (:clothingSize IS NULL OR EXISTS (SELECT 1 FROM Clothing WHERE :clothingSize IN (si.size))) AND" +
            " (:clothingColor IS NULL OR EXISTS (SELECT 1 FROM Clothing WHERE :clothingColor IN (col.id))) AND" +
            " (:isHot IS NULL OR c.isHot = :isHot) AND" +
            " (:brand IS NULL OR c.brand = :brand) AND" +
            " (:description IS NULL OR c.description LIKE :description%) AND" +
            " (:rating IS NULL OR :rating = (SELECT CAST(AVG(r.rating) AS int) FROM Review r WHERE r.clothing.id = c.id))"
    )
    Page<Clothing> searchClothing(
            @Param("clothingSize") Integer size,
            @Param("clothingColor") Integer color,
            @Param("isHot") Boolean isHot,
            @Param("brand") String brand,
            @Param("description") String description,
            @Param("rating") Integer rating,
            Pageable pageable
    );

}
