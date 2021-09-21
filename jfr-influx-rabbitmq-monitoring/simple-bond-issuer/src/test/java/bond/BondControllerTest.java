package bond;

import bond.controller.BondController;
import bond.data.RequestBodies;
import bond.data.TestData;
import bond.domain.Bond;
import bond.messaging.event.BondIssuedTransactionProducer;
import bond.repository.BondHistoryRepository;
import bond.repository.BondRepository;
import bond.service.BondHistoryService;
import bond.service.BondService;
import bond.service.CouponProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(webDriverEnabled = false, webClientEnabled = false)
@WebMvcTest(value = {BondController.class, BondService.class, BondHistoryService.class})
public class BondControllerTest {

    @Value("${app.api.prefix}")
    private String apiPrefix;

    // tests web + service layer in spring terminology, so the service classes are actual instances injected into controller
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BondRepository bondRepository;
    @MockBean
    private BondHistoryRepository bondHistoryRepository;
    @MockBean
    private BondIssuedTransactionProducer bondIssuedTransactionProducer;

    @Test
    public void testListBondsByClientId() throws Exception {

        var clientId = 10L;
        int pageIndex = 0;
        int pageSize = 5;
        var pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        List<Bond> bonds = LongStream.range(0, 5).mapToObj(TestData::createBond).collect(Collectors.toList());
        Collections.reverse(bonds);

        given(bondRepository.searchBonds(clientId, pageRequest)).willReturn(new PageImpl<>(bonds, pageRequest, -1));

        mockMvc.perform(get(apiPrefix + "/bonds?clientId=" + clientId + "&sort=id,desc&size=5"))
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
                .andExpect(jsonPath("$.content").value(Matchers.hasSize(bonds.size() - pageSize * pageIndex)))
                .andExpect(jsonPath("$.content.*.bondId").value(contains(4, 3, 2, 1, 0)))
                .andExpect(jsonPath("$.content.*.clientId").value(contains(4, 3, 2, 1, 0)))
                .andExpect(jsonPath("$.content.*.term").value(contains(8, 6, 4, 2, 0)))
                .andExpect(jsonPath("$.content.*.interestRate").value(contains(12.0, 9.0, 6.0, 3.0, 0.0)))
                .andExpect(jsonPath("$.content.*.amount").value(contains("40", "30", "20", "10", "0")))
                .andExpect(jsonPath("$.content.*.createdDate").value(everyItem(equalTo(TestData.DUMMY_CREATED_DATE.toString()))))
                .andExpect(jsonPath("$.content.*.returnAmount").hasJsonPath());

        verify(bondRepository, times(1)).searchBonds(clientId, pageRequest);
    }

    @Test
    public void testCreateBond() throws Exception {

        var clientId = 100L;
        var term = 6;
        var amount = BigDecimal.valueOf(800L);
        var requestBody = RequestBodies.getBondRequest(clientId, term, amount);

        var bondCreated = new Bond();
        bondCreated.setId(999L);
        bondCreated.setCreatedDate(Instant.now());
        bondCreated.setTerm(requestBody.getTerm());
        bondCreated.setAmount(requestBody.getAmount());
        bondCreated.setClientId(requestBody.getClientId());
        bondCreated.setInterestRate(CouponProvider.DEFAULT_INTEREST_RATE);
        given(bondRepository.save(ArgumentMatchers.any())).willReturn(bondCreated);

        var requestBodyInBytes = new ObjectMapper().writeValueAsBytes(requestBody);

        mockMvc.perform(post(apiPrefix + "/bonds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyInBytes))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.issuedDate").exists())
                .andExpect(jsonPath("$.issuedDate").value(bondCreated.getCreatedDate().toString()))
                .andExpect(jsonPath("$.bondId").isNumber())
                .andExpect(jsonPath("$.bondId").value(bondCreated.getId()))
                .andExpect(jsonPath("$.clientId").isNumber())
                .andExpect(jsonPath("$.clientId").value(bondCreated.getClientId()))
                .andExpect(jsonPath("$.term").isNumber())
                .andExpect(jsonPath("$.term").value(bondCreated.getTerm()))
                .andExpect(jsonPath("$.interestRate").isNumber())
                .andExpect(jsonPath("$.interestRate").value(bondCreated.getInterestRate()))
                .andExpect(jsonPath("$.amount").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(bondCreated.getAmount()))
                .andExpect(jsonPath("$.returnAmount").value(CouponProvider.calculateReturnAmount(
                        bondCreated.getAmount(), bondCreated.getTerm(), bondCreated.getInterestRate()
                )));

        verify(bondRepository, times(1)).save(ArgumentMatchers.any());
    }

    @Test
    public void testUpdateBond() throws Exception {

        var bondId = 888L;
        var termToUpdate = 10;
        var requestBody = RequestBodies.getBondRequest(0, termToUpdate, null);

        var clientId = 50L;
        var amount = BigDecimal.valueOf(500);
        var bondToUpdate = TestData.createBond(bondId, clientId, 111111, CouponProvider.DEFAULT_INTEREST_RATE, amount);
        given(bondRepository.findById(bondId)).willReturn(Optional.of(bondToUpdate));

        var bondUpdated = TestData.createBond(bondId, clientId, termToUpdate, CouponProvider.DEFAULT_INTEREST_RATE, amount);
        given(bondRepository.save(ArgumentMatchers.any())).willReturn(bondUpdated);

        var requestBodyInBytes = new ObjectMapper().writeValueAsBytes(requestBody);

        mockMvc.perform(patch(apiPrefix + "/bonds/" + bondId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyInBytes))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.issuedDate").exists())
                .andExpect(jsonPath("$.issuedDate").value(bondUpdated.getCreatedDate().toString()))
                .andExpect(jsonPath("$.bondId").isNumber())
                .andExpect(jsonPath("$.bondId").value(bondId))
                .andExpect(jsonPath("$.clientId").isNumber())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.term").isNumber())
                .andExpect(jsonPath("$.term").value(termToUpdate))
                .andExpect(jsonPath("$.amount").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(amount))
                .andExpect(jsonPath("$.interestRate").isNumber())
                .andExpect(jsonPath("$.interestRate").value(bondUpdated.getInterestRate()))
                .andExpect(jsonPath("$.returnAmount").value(CouponProvider.calculateReturnAmount(
                        amount, termToUpdate, bondUpdated.getInterestRate()
                )));

        verify(bondRepository, times(1)).findById(ArgumentMatchers.any());
        verify(bondRepository, times(1)).save(ArgumentMatchers.any());
    }
}
