package bond.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import bond.domain.Bond;

public interface BondRepository extends CrudRepository<Bond, Long> {

    /**
     * @param clientId client that the bonds are issued for.
     */
    @Query("SELECT b FROM Bond b WHERE (:clientId IS NULL OR b.clientId = :clientId)")
    Page<Bond> searchBonds(@Param("clientId") Long clientId,
                           Pageable pageable);

    /**
     * selects the count of total issued bonds at the current date for a client and IP address.
     */
    @Query("SELECT COUNT(b) FROM Bond b" +
            " WHERE b.sourceIp = :sourceIp" +
            " AND b.clientId = :clientId" +
            " AND year(b.createdDate) = year(current_date())" +
            " AND month(b.createdDate) = month(current_date())" +
            " AND day(b.createdDate) = day(current_date())")
    Integer countSoldBondsForClientAndSourceIp(@Param("clientId") Long clientId,
                                               @Param("sourceIp") String sourceIp);

}
