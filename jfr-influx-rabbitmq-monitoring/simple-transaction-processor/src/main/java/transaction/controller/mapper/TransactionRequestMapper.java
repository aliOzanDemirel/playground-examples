package transaction.controller.mapper;

import lombok.extern.slf4j.Slf4j;
import transaction.controller.request.TransactionRequest;
import transaction.domain.Transaction;
import transaction.exception.InvalidInputException;
import transaction.util.Utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class TransactionRequestMapper {

    public static final List<String> TRANSACTION_TIMESTAMP_INPUT_FORMATS = new ArrayList<>(4);

    static {
        TRANSACTION_TIMESTAMP_INPUT_FORMATS.add("yyyy-MM-dd'T'HH:mm:ss'Z'");
        TRANSACTION_TIMESTAMP_INPUT_FORMATS.add("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        TRANSACTION_TIMESTAMP_INPUT_FORMATS.add("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        TRANSACTION_TIMESTAMP_INPUT_FORMATS.add("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'");
    }

    private TransactionRequestMapper() {
    }

    /**
     * @param body TransactionReqBody object to be converted to Transaction
     * @return Transaction domain object that will be saved in cache
     */
    public static Transaction convertTransactionRequestBody(TransactionRequest body) throws Exception {

        Transaction transaction = new Transaction();
        try {
            transaction.setAmount(BigDecimal.valueOf(Double.parseDouble(body.getAmount())));
        } catch (NumberFormatException e) {
            Utils.logAndThrowException("Invalid amount: " + body.getAmount(), InvalidInputException.class);
        }
        long timestampInMillis = timestampInputToMillis(body.getTimestamp());
        transaction.setTimestamp(timestampInMillis);
        return transaction;
    }

    /**
     * @param timestampStr see {@link TransactionRequestMapper#TRANSACTION_TIMESTAMP_INPUT_FORMATS}
     *                     for allowed input formats
     * @return epoch milliseconds of given UTC timestamp
     * @throws InvalidInputException if timestamp could not be parsed with allowed input formats
     */
    public static long timestampInputToMillis(String timestampStr) {

        return TRANSACTION_TIMESTAMP_INPUT_FORMATS.stream()
                .map(it -> {
                    try {
                        return LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern(it));
                    } catch (DateTimeParseException e) {
                        // ignore since the other format could fit
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(ldt -> ldt.toInstant(ZoneOffset.UTC).toEpochMilli())
                .findFirst()
                .orElseThrow(() -> {
                    String err = "Invalid timestamp: " + timestampStr;
                    return new InvalidInputException(err);
                });
    }
}
