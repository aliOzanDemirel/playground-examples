package clothing.service.dto.response;

import clothing.service.domain.Review;
import clothing.service.dto.JsonViews;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClothingReviewResponse {

    public ClothingReviewResponse(Review review) {

        this(review, false);
    }

    public ClothingReviewResponse(Review review, boolean withClothingInfo) {

        id = review.getId();
        rating = review.getRating();
        description = review.getDescription();

        if (withClothingInfo) {
            clothingId = review.getClothing().getId();
            clothingIsClosedToReview = review.getClothing().isClosedToReview();
            clothingIsHot = review.getClothing().isHot();
        }
    }

    private Long id;
    private Integer rating;
    private String description;

    @JsonView(JsonViews.PostBody.class)
    private Long clothingId;

    @JsonView(JsonViews.PostBody.class)
    private boolean clothingIsClosedToReview;

    @JsonView(JsonViews.PostBody.class)
    private boolean clothingIsHot;

}
