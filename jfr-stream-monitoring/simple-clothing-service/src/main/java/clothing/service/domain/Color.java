package clothing.service.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
public class Color extends IdAndHashingStrategy {

    // more colors can later be supported for specific clothings, hence a dynamic table

    @Column(nullable = false)
    @NotBlank
    private String value;

    // one to many bags should not be fetched EAGERly, this is for the sake of simplicity
    @OneToMany(mappedBy = "color", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<ClothingColor> clothingColors = new ArrayList<>();

}
