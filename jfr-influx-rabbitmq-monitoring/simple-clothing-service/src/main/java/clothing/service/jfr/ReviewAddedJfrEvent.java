package clothing.service.jfr;

import jdk.jfr.*;

@Name("clothing.ClothingReviewAdded")
@Label("Review Added Event")
@Description("Tracks when a new review is made for clothing")
@StackTrace(false)
public class ReviewAddedJfrEvent extends Event {

    @Label("Success Flag")
    private boolean isSuccess = false;

    @Label("Review Rating")
    private int rating = -1;

    @Label("Clothing Brand")
    private String clothingBrand;

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getClothingBrand() {
        return clothingBrand;
    }

    public void setClothingBrand(String clothingBrand) {
        this.clothingBrand = clothingBrand;
    }
}
