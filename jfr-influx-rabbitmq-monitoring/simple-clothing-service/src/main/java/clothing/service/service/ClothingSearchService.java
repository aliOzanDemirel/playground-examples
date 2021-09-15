package clothing.service.service;

import clothing.service.domain.Clothing;
import clothing.service.domain.ClothingSize;
import clothing.service.repository.ClothingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClothingSearchService {

    private final ClothingRepository clothingRepository;

    @Autowired
    public ClothingSearchService(ClothingRepository clothingRepository) {
        this.clothingRepository = clothingRepository;
    }

    public Page<Clothing> findClothingByFiltering(ClothingSize.Size size, Integer clothingColorId, Boolean isHot, String brand,
                                                  String description, Integer rating, Pageable pageable) {

        var clSize = size != null ? size.ordinal() : null;
        var clothingPage = clothingRepository.searchClothing(clSize, clothingColorId, isHot, brand, description, rating, pageable);

        log.debug("Fetched {} clothing with parameters size: {}, color: {}, isHot: {}, brand: {}, desc: {}, rating: {}",
                clothingPage.getTotalElements(), size, clothingColorId, isHot, brand, description, rating);

        return clothingPage;
    }
}
