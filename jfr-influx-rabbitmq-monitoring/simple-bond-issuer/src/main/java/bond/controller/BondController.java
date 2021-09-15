package bond.controller;

import bond.dto.JsonViews;
import bond.dto.request.BondRequest;
import bond.dto.wrapper.ListResponse;
import bond.dto.wrapper.PageResponse;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import bond.config.swagger.PagingParameters;
import bond.dto.response.BondHistoryResponse;
import bond.dto.response.BondResponse;
import bond.helper.IpAddressResolver;
import bond.service.BondHistoryService;
import bond.service.BondService;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${app.api.prefix}/bonds")
@Slf4j
public class BondController {

    private BondService bondService;
    private BondHistoryService bondHistoryService;

    @Autowired
    public BondController(BondService bondService, BondHistoryService bondHistoryService) {
        this.bondService = bondService;
        this.bondHistoryService = bondHistoryService;
    }

    @PostMapping
    @ApiOperation(value = "Creates a new bond for the given client, term and base amount.")
    public BondResponse createBond(
            HttpServletRequest request,
            @Validated(JsonViews.PostRequestBody.class) @JsonView(JsonViews.PostRequestBody.class) @RequestBody BondRequest body
    ) {
        return new BondResponse(
                bondService.createBond(
                        body.getClientId(),
                        body.getTerm(),
                        body.getAmount(),
                        IpAddressResolver.getClientIpAddress(request)
                )
        );
    }

    @PatchMapping("/{id}")
    @ApiOperation(value = "Updates a bond's term and interest rate.")
    public BondResponse updateBond(
            @PathVariable @Min(1) Long id,
            @Validated(JsonViews.PatchRequestBody.class) @JsonView(JsonViews.PatchRequestBody.class) @RequestBody BondRequest body
    ) {
        return new BondResponse(
                bondService.updateBond(id, body.getTerm())
        );
    }

    @GetMapping
    @PagingParameters
    @ApiOperation(value = "Fetches bonds by applying pagination. Default page has 10 elements. Bonds can be filtered by clientId.")
    public PageResponse<BondResponse> listBonds(@RequestParam(required = false) @Min(1) Long clientId,
                                                @ApiIgnore Pageable pageable) {

        return new PageResponse<>(bondService.findBonds(clientId, pageable).map(BondResponse::new));
    }

    @GetMapping("/{id}/history")
    @ApiOperation(value = "Fetches the saved history of bond when its term is updated.")
    public ListResponse<BondHistoryResponse> listBondHistory(@PathVariable @Min(1) Long id) {

        return new ListResponse<>(
                bondHistoryService.getHistoryRecordsOfBond(id).stream().map(BondHistoryResponse::new).collect(Collectors.toList())
        );
    }

}
