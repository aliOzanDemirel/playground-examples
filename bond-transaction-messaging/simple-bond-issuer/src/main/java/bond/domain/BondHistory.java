package bond.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
public class BondHistory extends IdAndHashingStrategy {

    /**
     * simple auditing just for the term and interest rate of bond, nothing generic here
     */

    @JoinColumn(name = "bond_id", nullable = false, foreignKey = @ForeignKey(name = "FK_BH_BOND"))
    @ManyToOne
    @NotNull
    private Bond bond;

    @Column(nullable = false)
    @NotNull
    private Integer term;

    @Column(nullable = false)
    @NotNull
    private Double interestRate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Action action;

    public enum Action {
        CREATED,
        UPDATED
    }

}
