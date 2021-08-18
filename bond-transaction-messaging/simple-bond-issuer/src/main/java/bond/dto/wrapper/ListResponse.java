package bond.dto.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListResponse<T> implements ContentWrapper {

    private List<T> content = Collections.emptyList();

}
