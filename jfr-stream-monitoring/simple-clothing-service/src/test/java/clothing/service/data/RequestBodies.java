package clothing.service.data;

import clothing.service.dto.request.ClothingReviewRequest;

public class RequestBodies {

    private RequestBodies() {
    }

    public static ClothingReviewRequest reviewRequestBody(String description, int rating) {

        var body = new ClothingReviewRequest();
        body.setDescription(description);
        body.setRating(rating);
        return body;
    }

}
