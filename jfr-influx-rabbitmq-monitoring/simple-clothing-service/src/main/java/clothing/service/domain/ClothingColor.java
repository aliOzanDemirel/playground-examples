package clothing.service.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
public class ClothingColor extends IdAndHashingStrategy {

    @JoinColumn(name = "clothing_id", nullable = false, foreignKey = @ForeignKey(name = "FK_CC_CLOTHING"))
    @ManyToOne
    @NotNull
    private Clothing clothing;

    @JoinColumn(name = "color_id", nullable = false, foreignKey = @ForeignKey(name = "FK_CC_COLOR"))
    @ManyToOne
    @NotNull
    private Color color;

}
