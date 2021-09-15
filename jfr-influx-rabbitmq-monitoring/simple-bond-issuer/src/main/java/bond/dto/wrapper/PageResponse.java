package bond.dto.wrapper;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PageResponse<T> implements ContentWrapper {

    public PageResponse(Page<T> generatedPage) {

        content = generatedPage.getContent();

        page.size = generatedPage.getSize();
        page.number = generatedPage.getNumber();
        page.totalPages = generatedPage.getTotalPages();
        page.totalElements = generatedPage.getTotalElements();
    }

    private List<T> content = new ArrayList<>();
    private PageAttributes page = new PageAttributes();

    @Getter
    private static class PageAttributes {

        private int size;
        private int number;
        private int totalPages;
        private long totalElements;
    }

}
