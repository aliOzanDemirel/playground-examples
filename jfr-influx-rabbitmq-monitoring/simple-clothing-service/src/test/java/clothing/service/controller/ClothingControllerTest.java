package clothing.service.controller;

import clothing.service.data.RequestBodies;
import clothing.service.data.TestData;
import clothing.service.domain.Clothing;
import clothing.service.domain.ClothingSize;
import clothing.service.domain.Review;
import clothing.service.repository.ClothingRepository;
import clothing.service.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@RunWith(SpringRunner.class)
//@AutoConfigureMockMvc(webDriverEnabled = false, webClientEnabled = false)
//@WebMvcTest(value = {ClothingController.class, ClothingSearchService.class, ReviewAddService.class})
@Disabled
public class ClothingControllerTest {

    @Value("${app.api.prefix}")
    private String apiPrefix;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClothingRepository clothingRepository;
    @MockBean
    private ReviewRepository reviewRepository;

    @Test
    public void testListClothing() throws Exception {

        List<Clothing> clothing = LongStream.range(0, 5).mapToObj(TestData::createClothing).collect(Collectors.toList());
        Collections.reverse(clothing);

        var size = ClothingSize.Size.LARGE;
        var colorId = 1;
        boolean isHot = true;
        int pageIndex = 0;
        int pageSize = 5;
        var pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        given(clothingRepository.searchClothing(size.ordinal(), colorId, isHot, null, null, null, pageRequest))
                .willReturn(new PageImpl<>(clothing, pageRequest, -1));

        mockMvc.perform(get(apiPrefix + "/clothing?sort=id,desc&size=5&clSize=" + size
                + "&clColor=" + colorId + "&isHot=" + isHot))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").isMap())
                .andExpect(jsonPath("$.page.number").isNumber())
                .andExpect(jsonPath("$.page.number").value(pageIndex))
                .andExpect(jsonPath("$.page.size").isNumber())
                .andExpect(jsonPath("$.page.size").value(pageSize))
                .andExpect(jsonPath("$.page.totalPages").isNumber())
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.totalElements").isNumber())
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").value(Matchers.hasSize(clothing.size() - pageSize * pageIndex)))
                .andExpect(jsonPath("$.content.*.id").value(contains(4, 3, 2, 1, 0)))
                .andExpect(jsonPath("$.content.*.isHot").value(everyItem(equalTo(true))))
                .andExpect(jsonPath("$.content.*.isClosedToReview").value(contains(true, false, true, false, true)))
                .andExpect(jsonPath("$.content.*.description").value(contains("Desc-4", "Desc-3", "Desc-2", "Desc-1", "Desc-0")))
                .andExpect(jsonPath("$.content.*.brand").value(contains("Brand-4", "Brand-3", "Brand-2", "Brand-1", "Brand-0")))
                .andExpect(jsonPath("$.content.*.availableColors").value(everyItem(contains("SomeColor-10", "SomeColor-20"))))
                .andExpect(jsonPath("$.content.*.availableSizes").value(everyItem(contains("LARGE", "SMALL"))));

        verify(clothingRepository, times(1)).searchClothing(size.ordinal(), colorId, isHot, null, null, null, pageRequest);
    }

    @Test
    public void testAddReviewSuccessfully() throws Exception {

        var clothingId = 111L;
        var clothing = TestData.createClothing(clothingId, false, false, "ReviewWillBeAdded", "Brand");

        List<Review> reviews = Arrays.asList(
                TestData.createReview(1, clothing, 5),
                TestData.createReview(2, clothing, 4),
                TestData.createReview(3, clothing, 3),
                TestData.createReview(4, clothing, 3)
        );
        given(reviewRepository.findByClothingId(clothingId)).willReturn(reviews);
        given(clothingRepository.findById(clothingId)).willReturn(Optional.of(clothing));
        given(clothingRepository.save(ArgumentMatchers.any())).willReturn(clothing);

        var reviewToBeCreated = TestData.createReview(5, clothing, 5);
        given(reviewRepository.save(ArgumentMatchers.any())).willReturn(reviewToBeCreated);

        var reqDesc = "Review-5";
        var reqRating = 5;
        var requestBody = RequestBodies.reviewRequestBody(reqDesc, reqRating);
        var requestBodyInBytes = new ObjectMapper().writeValueAsBytes(requestBody);

        mockMvc.perform(post(apiPrefix + "/clothing/" + clothingId + "/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyInBytes))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").value(reviewToBeCreated.getId()))
                .andExpect(jsonPath("$.description").isString())
                .andExpect(jsonPath("$.description").value(reviewToBeCreated.getDescription()))
                .andExpect(jsonPath("$.rating").isNumber())
                .andExpect(jsonPath("$.rating").value(reviewToBeCreated.getRating()))
                .andExpect(jsonPath("$.clothingId").hasJsonPath())
                .andExpect(jsonPath("$.clothingId").isNumber())
                .andExpect(jsonPath("$.clothingId").value(clothingId))
                .andExpect(jsonPath("$.clothingIsClosedToReview").hasJsonPath())
                .andExpect(jsonPath("$.clothingIsClosedToReview").isBoolean())
                .andExpect(jsonPath("$.clothingIsClosedToReview").value(false))
                .andExpect(jsonPath("$.clothingIsHot").hasJsonPath())
                .andExpect(jsonPath("$.clothingIsHot").isBoolean())
                .andExpect(jsonPath("$.clothingIsHot").value(true));

        verify(clothingRepository, times(1)).findById(clothingId);
        verify(reviewRepository, times(1)).findByClothingId(clothingId);
        verify(clothingRepository, times(1)).save(ArgumentMatchers.any());
        verify(reviewRepository, times(1)).save(ArgumentMatchers.any());
    }

    @Test
    public void testReviewIsNotPermittedForClothing() throws Exception {

        var clothingId = 999L;
        var clothing = TestData.createClothing(clothingId, false, true, "ReviewIsAlreadyClosed", "Brand");
        given(clothingRepository.findById(clothingId)).willReturn(Optional.of(clothing));

        var requestBody = RequestBodies.reviewRequestBody("WillBeDiscarded", 1);
        var requestBodyInBytes = new ObjectMapper().writeValueAsBytes(requestBody);

        mockMvc.perform(post(apiPrefix + "/clothing/" + clothingId + "/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyInBytes))
                .andExpect(status().is5xxServerError());

        verify(clothingRepository, times(1)).findById(clothingId);
        verify(reviewRepository, never()).findByClothingId(clothingId);
        verify(clothingRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    public void testListReviews() throws Exception {

        var clothingId = 5L;
        List<Review> reviews = LongStream.range(0, 5).mapToObj(id -> TestData.createReview(id, clothingId)).collect(Collectors.toList());
        given(reviewRepository.findByClothingId(clothingId)).willReturn(reviews);

        mockMvc.perform(get(apiPrefix + "/clothing/" + clothingId + "/reviews"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").value(Matchers.hasSize(reviews.size())))
                .andExpect(jsonPath("$.content.*.id").value(contains(0, 1, 2, 3, 4)))
                .andExpect(jsonPath("$.content.*.rating").value(contains(0, 1, 2, 3, 4)))
                .andExpect(jsonPath("$.content.*.description").value(contains("Review-0", "Review-1", "Review-2", "Review-3", "Review-4")))
                .andExpect(jsonPath("$.content.*.clothingId").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content.*.clothingIsClosedToReview").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content.*.clothingIsHot").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content[?(@.description === 'SomeRandomReview')]").doesNotExist())
                .andExpect(jsonPath("$.content[?(@.rating === 5)]").doesNotExist());

        verify(reviewRepository, times(1)).findByClothingId(clothingId);
    }
}
