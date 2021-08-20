package clothing.service.data;

import clothing.service.domain.*;
import clothing.service.domain.ClothingSize.Size;

import java.util.List;

public class TestData {

    private TestData() {
    }

    public static Review createReview(long id, long clothingId) {

        var clothing = createClothing(clothingId);
        return createReview(id, clothing, (int) id % 5);
    }

    public static Review createReview(long id, Clothing clothing, int rating) {

        var entity = new Review();
        entity.setId(id);
        entity.setClothing(clothing);
        entity.setDescription("Review-" + id);
        entity.setRating(rating);
        return entity;
    }

    public static Clothing createClothing(long id) {

        return createClothing(id, true, (id % 2) == 0, "Desc-" + id, "Brand-" + id);
    }

    public static Clothing createClothing(long id, boolean isHot, boolean closedToReview, String desc, String brand) {

        var entity = new Clothing();
        entity.setId(id);
        entity.setHot(isHot);
        entity.setClosedToReview(closedToReview);
        entity.setDescription(desc);
        entity.setBrand(brand);
        entity.setClothingColors(List.of(createClothingColor(entity, 10L), createClothingColor(entity, 20L)));
        entity.setClothingSizes(List.of(createClothingSize(entity, Size.LARGE), createClothingSize(entity, Size.SMALL)));
        return entity;
    }

    private static ClothingSize createClothingSize(Clothing clothing, Size size) {

        var entity = new ClothingSize();
        entity.setClothing(clothing);
        entity.setSize(size);
        return entity;
    }

    private static ClothingColor createClothingColor(Clothing clothing, Long colorId) {

        var entity = new ClothingColor();
        entity.setClothing(clothing);
        entity.setColor(createColor(colorId));
        return entity;
    }

    private static Color createColor(Long id) {

        var entity = new Color();
        entity.setId(id);
        entity.setValue("SomeColor-" + id);
        return entity;
    }

}
