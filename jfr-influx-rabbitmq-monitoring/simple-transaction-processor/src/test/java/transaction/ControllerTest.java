package transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import transaction.controller.StatisticsController;
import transaction.controller.TransactionController;
import transaction.controller.response.StatisticsResponse;
import transaction.domain.Transaction;
import transaction.exception.OldTransactionException;
import transaction.service.TransactionService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static transaction.controller.mapper.TransactionRequestMapper.convertTransactionRequestBody;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {TransactionController.class, StatisticsController.class})
public class ControllerTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    // integration testing of web layer only, executes against real controller beans with injected mock bean
    @Autowired
    private MockMvc mvc;
    @MockBean
    private TransactionService transactionService;

    @Before
    public void before() throws Exception {

        doAnswer(i -> null).when(transactionService).deleteAllTransactions();
        doAnswer(i -> null).when(transactionService).saveTransaction(any(Transaction.class));
        given(transactionService.getTransactionStatistics()).willReturn(new StatisticsResponse());
    }

    @Test
    public void testGetStatistics() throws Exception {

        mvc.perform(get(StatisticsController.STATISTICS_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.max").exists())
                .andExpect(jsonPath("$.min").exists())
                .andExpect(jsonPath("$.sum").exists())
                .andExpect(jsonPath("$.avg").exists());

        verify(transactionService, times(1)).getTransactionStatistics();
    }

    @Test
    public void testDeleteTransactions() throws Exception {

        mvc.perform(delete(TransactionController.TRANSACTIONS_ENDPOINT))
                .andExpect(status().isNoContent());

        verify(transactionService, times(1)).deleteAllTransactions();
    }

    @Test
    public void testSaveTransaction() throws Exception {

        mvc.perform(post(TransactionController.TRANSACTIONS_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(RequestBodies.REQ_VALID)))
                .andExpect(status().isCreated());

        doThrow(OldTransactionException.class)
                .when(transactionService).saveTransaction(convertTransactionRequestBody(RequestBodies.REQ_TIMESTAMP_OLD_DATE));
        mvc.perform(post(TransactionController.TRANSACTIONS_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(RequestBodies.REQ_TIMESTAMP_OLD_DATE)))
                .andExpect(status().isNoContent());

        mvc.perform(post(TransactionController.TRANSACTIONS_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(RequestBodies.REQ_AMOUNT_INVALID)))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(post(TransactionController.TRANSACTIONS_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(RequestBodies.REQ_TIMESTAMP_INVALID_DATE)))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(post(TransactionController.TRANSACTIONS_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(RequestBodies.REQ_TIMESTAMP_INVALID_FORMAT)))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(post(TransactionController.TRANSACTIONS_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(RequestBodies.REQ_INVALID_JSON)))
                .andExpect(status().isBadRequest());

        verify(transactionService, times(2)).saveTransaction(any(Transaction.class));
    }
}
