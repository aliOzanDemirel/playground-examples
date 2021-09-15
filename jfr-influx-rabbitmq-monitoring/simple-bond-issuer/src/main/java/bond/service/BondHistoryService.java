package bond.service;

import bond.repository.BondHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import bond.domain.Bond;
import bond.domain.BondHistory;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class BondHistoryService {

    private BondHistoryRepository bondHistoryRepository;

    @Autowired
    public BondHistoryService(BondHistoryRepository bondHistoryRepository) {
        this.bondHistoryRepository = bondHistoryRepository;
    }

    public List<BondHistory> getHistoryRecordsOfBond(Long bondId) {

        var histories = bondHistoryRepository.findByBondId(bondId);
        log.debug("Fetched {} history records for bond: {}", histories.size(), bondId);
        return histories;
    }

    BondHistory historyForCreatedBond(Bond bond) {

        var history = newHistoryRecord(bond);
        history.setAction(BondHistory.Action.CREATED);
        return history;
    }

    BondHistory historyForUpdatedBond(Bond bond) {

        var history = newHistoryRecord(bond);
        history.setAction(BondHistory.Action.UPDATED);
        return history;
    }

    private BondHistory newHistoryRecord(Bond bond) {

        var history = new BondHistory();
        history.setBond(bond);
        history.setCreatedDate(Instant.now());
        history.setTerm(bond.getTerm());
        history.setInterestRate(bond.getInterestRate());
        return history;
    }
}
