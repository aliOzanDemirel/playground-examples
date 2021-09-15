package clothing.service.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
public class ClothingSize extends IdAndHashingStrategy {

    @JoinColumn(name = "clothing_id", nullable = false, foreignKey = @ForeignKey(name = "FK_CS_CLOTHING"))
    @ManyToOne
    @NotNull
    private Clothing clothing;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    @NotNull
    private Size size;

    public enum Size {
        SMALL, MEDIUM, LARGE
    }

}
