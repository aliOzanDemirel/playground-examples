package bond.dto.request;

import bond.dto.JsonViews;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static bond.service.CouponProvider.DEFAULT_MINIMUM_TERM;

@Data
public class BondRequest {

    @JsonView(JsonViews.PostRequestBody.class)
    @NotNull(message = "Client ID must be provided!", groups = JsonViews.PostRequestBody.class)
    @Min(value = 1, message = "Client ID should be positive number!", groups = JsonViews.PostRequestBody.class)
    private Long clientId;

    @NotNull(message = "Term must be provided!",
            groups = {JsonViews.PostRequestBody.class, JsonViews.PatchRequestBody.class})
    @Min(value = DEFAULT_MINIMUM_TERM, message = "Term cannot be less than 5 years!",
            groups = {JsonViews.PostRequestBody.class, JsonViews.PatchRequestBody.class})
    private Integer term;

    @JsonView(JsonViews.PostRequestBody.class)
    @NotNull(message = "Amount must be provided!", groups = JsonViews.PostRequestBody.class)
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount should be positive number!", groups = JsonViews.PostRequestBody.class)
    @Digits(integer = 8, fraction = 2, groups = JsonViews.PostRequestBody.class,
            message = "Amount's integer part can have at most 8 characters while the scale should be 2!")
    private BigDecimal amount;

//    can have some coupon identifier to support multiple possible coupons with different interest rates
//    discarded for simplicity as there is only one default coupon in application
//    private String couponId;

}
