package clothing.service.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ClothingReviewRequest {

    @NotBlank(message = "Review text must be provided!")
    @Length(max = 2000, message = "Review text cannot be more than 2000 characters!")
    private String description;

    @NotNull(message = "Rating must be provided!")
    @Min(value = 1, message = "Rating cannot be less than 1 star!")
    @Max(value = 5, message = "Rating cannot be more than 5 stars!")
    private Integer rating;

}
