package bond.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import bond.domain.BondHistory;

import java.util.List;

@Repository
public interface BondHistoryRepository extends CrudRepository<BondHistory, Long> {

    @Query("SELECT bh FROM BondHistory bh WHERE bh.bond.id = :bondId")
    List<BondHistory> findByBondId(@Param("bondId") Long bondId);

}
