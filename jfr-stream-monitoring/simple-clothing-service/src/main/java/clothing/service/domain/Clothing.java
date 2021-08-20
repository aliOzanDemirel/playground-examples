package clothing.service.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
public class Clothing extends IdAndHashingStrategy {

    @Column(length = 2000)
    private String description;

    // can just be status with different possible values like NORMAL, HOT, SMOKING_HOT
    @ColumnDefault("false")
    private boolean isHot = false;

    @ColumnDefault("false")
    private boolean isClosedToReview = false;

    // should probably be lookup with specific enumeration values, string for now for simplicity
    @Column(nullable = false)
    @NotBlank
    private String brand;

    // one to many bags should not be fetched EAGERly, this is for the sake of simplicity
    @OneToMany(mappedBy = "clothing", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<ClothingColor> clothingColors = new ArrayList<>();

    // one to many bags should not be fetched EAGERly, this is for the sake of simplicity
    @OneToMany(mappedBy = "clothing", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @ToString.Exclude
    private List<ClothingSize> clothingSizes = new ArrayList<>();

    @OneToMany(mappedBy = "clothing")
    @ToString.Exclude
    private List<Review> reviews = new ArrayList<>();

}
