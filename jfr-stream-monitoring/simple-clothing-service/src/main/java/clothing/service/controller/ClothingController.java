package clothing.service.controller;

import clothing.service.config.swagger.PagingParameters;
import clothing.service.domain.ClothingSize;
import clothing.service.dto.JsonViews;
import clothing.service.dto.request.ClothingReviewRequest;
import clothing.service.dto.response.ClothingResponse;
import clothing.service.dto.response.ClothingReviewResponse;
import clothing.service.dto.wrapper.ListResponse;
import clothing.service.dto.wrapper.PageResponse;
import clothing.service.service.ClothingSearchService;
import clothing.service.service.ReviewAddService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${app.api.prefix}/clothing")
public class ClothingController {

    private final ClothingSearchService clothingSearchService;
    private final ReviewAddService reviewAddService;

    @Autowired
    public ClothingController(ClothingSearchService clothingSearchService, ReviewAddService reviewAddService) {
        this.clothingSearchService = clothingSearchService;
        this.reviewAddService = reviewAddService;
    }

    @GetMapping
    @PagingParameters
    @ApiOperation(value = "Fetches articles of clothing by paginating response. Default page has 10 elements. " +
            "Filtering parameters are: clSize, clColor, isHot, brand, description, rating.")
    public PageResponse<ClothingResponse> listClothing(@RequestParam(required = false) ClothingSize.Size clSize,
                                                       @RequestParam(required = false) Integer clColor,
                                                       @RequestParam(required = false) Boolean isHot,
                                                       @RequestParam(required = false) String brand,
                                                       @RequestParam(required = false) String description,
                                                       @RequestParam(required = false) Integer rating,
                                                       @ApiIgnore Pageable pageable) {

        return new PageResponse<>(
                clothingSearchService.findClothingByFiltering(clSize, clColor, isHot, brand, description, rating, pageable)
                        .map(ClothingResponse::new)
        );
    }

    @GetMapping("/{clothingId}/reviews")
    @ApiOperation(value = "Lists the reviews of the clothing.")
    public MappingJacksonValue listReviewsOfClothing(@PathVariable @Min(1) Long clothingId) {

        MappingJacksonValue value = new MappingJacksonValue(
                new ListResponse<>(
                        reviewAddService.getReviewsOfClothing(clothingId).stream()
                                .map(ClothingReviewResponse::new)
                                .collect(Collectors.toList())
                )
        );
        value.setSerializationView(JsonViews.GetBody.class);
        return value;
    }

    @PostMapping("/{clothingId}/reviews")
    @ApiOperation(value = "Saves a review and rating star for the provided clothing.")
    public MappingJacksonValue saveReviewForClothing(@PathVariable @Min(1) Long clothingId,
                                                     @RequestBody @Valid ClothingReviewRequest requestBody) {

        var createdReview = reviewAddService.addReviewToClothing(clothingId, requestBody.getDescription(), requestBody.getRating());

        MappingJacksonValue value = new MappingJacksonValue(new ClothingReviewResponse(createdReview, true));
        value.setSerializationView(JsonViews.PostBody.class);
        return value;
    }

}
