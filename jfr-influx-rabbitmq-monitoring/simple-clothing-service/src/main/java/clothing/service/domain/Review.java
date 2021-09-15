package clothing.service.domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Review extends IdAndHashingStrategy {

    @Column(length = 2000, nullable = false)
    @NotBlank
    private String description;

    @Column(nullable = false)
    @NotNull
    @Min(value = 1)
    @Max(value = 5)
    private Integer rating;

    @JoinColumn(name = "clothing_id", nullable = false, foreignKey = @ForeignKey(name = "FK_REVIEW_CLOTHING"))
    @ManyToOne
    @NotNull
    private Clothing clothing;

}
