package bond.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
public class Bond extends IdAndHashingStrategy {

    // refers to some client's personal data
    @Column(nullable = false)
    @NotNull
    private Long clientId;

    // can have some end date instead of storing the term
    @Column(nullable = false)
    @NotNull
    @Min(value = 1)
    private Integer term;

    @Column(nullable = false)
    @NotNull
    private BigDecimal amount;

    @Column(nullable = false)
    @NotNull
    private Double interestRate;

    // can be null maybe? should be kept somewhere else?
    @Column(nullable = false)
    @NotBlank
    private String sourceIp;

    // timezone of creation date can also be kept, for now all are in UTC

    // one to many bags should not be fetched EAGERly, this is for the sake of simplicity
    @OneToMany(mappedBy = "bond", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<BondHistory> bondHistories = new ArrayList<>();

    public void addHistory(BondHistory bondHistory) {
        if (bondHistories != null && bondHistory != null) {
            bondHistories.add(bondHistory);
            bondHistory.setBond(this);
        }
    }
}
