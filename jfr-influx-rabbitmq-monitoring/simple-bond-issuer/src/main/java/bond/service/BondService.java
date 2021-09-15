package bond.service;

import bond.domain.Bond;
import bond.messaging.BondIssuedEventProducer;
import bond.repository.BondRepository;
import bond.validator.BondSaleValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Service
@Slf4j
@Validated
public class BondService {

    private final BondRepository bondRepository;
    private final BondHistoryService bondHistoryService;

    @Autowired(required = false)
    private BondIssuedEventProducer bondIssuedEventProducer;

    @Autowired
    public BondService(BondRepository bondRepository, BondHistoryService bondHistoryService) {
        this.bondRepository = bondRepository;
        this.bondHistoryService = bondHistoryService;
    }

    private Bond getBond(Long bondId) {

        log.debug("Fetching bond with ID: {}", bondId);
        return bondRepository.findById(bondId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bond is not found! ID: " + bondId));
    }

    public Page<Bond> findBonds(Long clientId, Pageable pageable) {

        var bondsInPage = bondRepository.searchBonds(clientId, pageable);
        log.debug("Fetched {} bonds for client ID: {}", bondsInPage.getTotalElements(), clientId);
        return bondsInPage;
    }

    /**
     * sells a new bond for the client with the specified term and amount.
     *
     * @param sourceIp used to determine if same client exceeded the limit by buying bonds in the same day.
     * @return newly sold bond.
     */
    @Transactional
    @BondSaleValidation
    public Bond createBond(Long clientId, Integer term, BigDecimal amount, String sourceIp) {

        log.debug("Creating new bond for client: {} with IP: {}, term: {} and amount: {}", clientId, sourceIp, term, amount);

        var bond = new Bond();
        bond.setClientId(clientId);
        bond.setSourceIp(sourceIp);
        bond.setTerm(term);
        bond.setAmount(amount);
        bond.setInterestRate(CouponProvider.DEFAULT_INTEREST_RATE);

        var history = bondHistoryService.historyForCreatedBond(bond);
        bond.addHistory(history);

        bond = saveBond(bond);
        log.debug("New bond is sold, ID: {}", bond.getId());

        if (bondIssuedEventProducer != null) {
            bondIssuedEventProducer.sendMessage(bond);
        }
        return bond;
    }

    /**
     * only updates the term of the bond and sets the new interest rate. if the term is extended, interest rate gets lower.
     *
     * @return updated bond if term is different.
     */
    @Transactional
    public Bond updateBond(Long bondId, Integer term) {

        var bond = getBond(bondId);
        log.debug("Updating bond: {} with term: {}", bondId, term);

        var bondShouldBeUpdated = !bond.getTerm().equals(term);
        if (bondShouldBeUpdated) {

            // if the term is extended
            if (bond.getTerm() < term) {
                bond.setInterestRate(CouponProvider.interestRateAfterTermExtension(bond.getInterestRate()));
            }
            bond.setTerm(term);

            var history = bondHistoryService.historyForUpdatedBond(bond);
            bond.addHistory(history);

            bond = saveBond(bond);
            log.debug("Bond is updated: {}", bond);

        } else {

            log.debug("Bond is not updated as it has the same term");
        }

        return bond;
    }

    private Bond saveBond(Bond bond) {

        try {
            return bondRepository.save(bond);

        } catch (Exception e) {

            var errMsg = "Error occurred while saving bond!" + (bond.getId() == null ? "" : " ID: " + bond.getId());
            log.error(errMsg, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errMsg);
        }
    }

}
