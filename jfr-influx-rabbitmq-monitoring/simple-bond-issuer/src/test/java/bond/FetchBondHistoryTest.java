package bond;

import bond.controller.BondController;
import bond.data.TestData;
import bond.domain.BondHistory;
import bond.repository.BondHistoryRepository;
import bond.service.BondHistoryService;
import bond.service.BondService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(webDriverEnabled = false, webClientEnabled = false)
@WebMvcTest(value = {BondController.class, BondHistoryService.class})
public class FetchBondHistoryTest {

    @Value("${app.api.prefix}")
    private String apiPrefix;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BondHistoryRepository bondHistoryRepository;
    @MockBean
    private BondService bondService;

    @Test
    public void testGetHistoryRecordsByBondId() throws Exception {

        var bondId = 100L;
        List<BondHistory> historyRecords = LongStream.range(0, 3)
                .mapToObj(it -> TestData.createBondHistory(it, bondId))
                .collect(Collectors.toList());

        given(bondHistoryRepository.findByBondId(bondId)).willReturn(historyRecords);

        mockMvc.perform(get(apiPrefix + "/bonds/" + bondId + "/history"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(historyRecords.size()))
                .andExpect(jsonPath("$.content.[*].id").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content.*.['createdDate']").value(everyItem(equalTo(TestData.DUMMY_CREATED_DATE.toString()))))
                .andExpect(jsonPath("$.content[*].action").value(hasItem(BondHistory.Action.UPDATED.name())))
                .andExpect(jsonPath("$.content..term").value(containsInAnyOrder(0, 2, 4)))
                .andExpect(jsonPath("$.content.*.['interestRate']").value(Matchers.contains(0.0, 3.0, 6.0)))
                .andExpect(jsonPath("$.content[1].interestRate").value(3d))
                .andExpect(jsonPath("$.content[2].term").value(4))
                .andExpect(jsonPath("$.content[?(@.interestRate === 6.0)]").exists())
                .andExpect(jsonPath("$.content[?(@.term === 25)]").doesNotExist())
                .andExpect(jsonPath("$.content[?(@.action === 'CREATED')]").doesNotExist());
    }

}
