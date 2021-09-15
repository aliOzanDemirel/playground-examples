package clothing.service.dto.response;

import clothing.service.domain.ClothingSize;
import lombok.Data;
import lombok.NoArgsConstructor;
import clothing.service.domain.Clothing;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ClothingResponse {

    public ClothingResponse(Clothing clothing) {

        id = clothing.getId();
        isHot = clothing.isHot();
        isClosedToReview = clothing.isClosedToReview();
        description = clothing.getDescription();
        brand = clothing.getBrand();
        availableColors = clothing.getClothingColors().stream().map(it -> it.getColor().getValue()).collect(Collectors.toList());
        availableSizes = clothing.getClothingSizes().stream().map(ClothingSize::getSize).collect(Collectors.toList());
//        reviews = clothing.getReviews().stream().map(ClothingReviewResponse::new).collect(Collectors.toList());
    }

    private Long id;
    private Boolean isHot;
    private Boolean isClosedToReview;
    private String description;
    private String brand;
    private List<String> availableColors;
    private List<ClothingSize.Size> availableSizes;

    // embedded here for simplicity, calls can be separated
//    private List<ClothingReviewResponse> reviews;

//    private static class ClothingRelations {
//        private String reviews;
//    }
}
