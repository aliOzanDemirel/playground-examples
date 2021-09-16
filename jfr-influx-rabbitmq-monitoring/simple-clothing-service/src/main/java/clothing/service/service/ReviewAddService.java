package clothing.service.service;

import clothing.service.domain.Clothing;
import clothing.service.domain.Review;
import clothing.service.jfr.ReviewAddedJfrEvent;
import clothing.service.messaging.review.ReviewAddedTransactionProducer;
import clothing.service.repository.ClothingRepository;
import clothing.service.repository.ReviewRepository;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAddService {

    // can be made configurable
    private static final int REVIEW_SIZE_THRESHOLD = 5;
    private static final int REVIEW_HOTNESS_RATING_THRESHOLD = 4;
    private static final int REVIEW_BLOCK_THRESHOLD = 2;

    private final ClothingRepository clothingRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewAddedTransactionProducer reviewAddedTransactionProducer;

    @Transactional
    public Review addReviewToClothing(long clothingId, String description, int rating) {

        log.debug("Adding review to clothing ID {}, description {} | rating {}", clothingId, description, rating);

        // event is enabled by the jfr agent running in same java process
        ReviewAddedJfrEvent event = new ReviewAddedJfrEvent();
        event.begin();

        try {
            var clothing = getClothing(clothingId);
            event.setClothingBrand(clothing.getBrand());

            if (clothing.isClosedToReview()) {
                throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "New review is not accepted for clothing with ID: " + clothingId);
            }

            Review review = addReviewToClothing(clothing, description, rating);
            reviewAddedTransactionProducer.sendMessage(review);

            event.setRating(rating);
            event.setSuccess(true);
            return review;

        } catch (Exception e) {

            var errMsg = "Error occurred while creating review: " + e.getMessage();
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, errMsg, e);

        } finally {
            event.commit();
        }
    }

    private Review addReviewToClothing(Clothing clothing, String description, Integer rating) {

        var reviews = getReviewsOfClothing(clothing.getId());
        var mutableReviews = Lists.newArrayList(reviews);

        var newReview = new Review(description, rating, clothing);
        mutableReviews.add(newReview);

        var shouldBeHot = reviewsConsideredHot(mutableReviews);
        var currentStatus = clothing.isHot();
        var updateClothing = shouldBeHot != currentStatus;

        // if status of clothing changes
        if (updateClothing) {

            clothing.setHot(shouldBeHot);
        }

        // if average is lower than 4, check for 2 as well to see if clothing review should be blocked
        if (!shouldBeHot && reviewsShouldBeBlocked(mutableReviews)) {

            clothing.setClosedToReview(true);
            updateClothing = true;
        }

        if (updateClothing) {

            log.debug("Updating clothing {} because of newly added review", clothing.getId());
            saveClothing(clothing);
        }

        return saveReview(newReview);
    }

    private boolean reviewsShouldBeBlocked(List<Review> reviews) {

        double average = getAverageRating(reviews);
        return reviews.size() >= REVIEW_SIZE_THRESHOLD && average >= 0 && average < REVIEW_BLOCK_THRESHOLD;
    }

    private boolean reviewsConsideredHot(List<Review> reviews) {

        double average = getAverageRating(reviews);
        return reviews.size() >= REVIEW_SIZE_THRESHOLD && average >= REVIEW_HOTNESS_RATING_THRESHOLD;
    }

    private double getAverageRating(List<Review> reviews) {

        return reviews.stream().mapToInt(Review::getRating).average().orElse(-1);
    }

    public List<Review> getReviewsOfClothing(Long clothingId) {

        log.debug("Fetching reviews of clothing: {}", clothingId);
        return reviewRepository.findByClothingId(clothingId);
    }

    private Clothing getClothing(Long clothingId) {

        log.debug("Fetching clothing: {}", clothingId);
        return clothingRepository.findById(clothingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clothing is not found! ID: " + clothingId));
    }

    private Review saveReview(Review review) {

        review = reviewRepository.save(review);
        log.debug("Review: {} is saved successfully.", review.getId());
        return review;
    }

    private void saveClothing(Clothing clothing) {

        clothing = clothingRepository.save(clothing);
        log.debug("Clothing: {} is saved successfully.", clothing.getId());
    }

}
