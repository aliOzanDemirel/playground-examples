package transaction.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Transaction {

    private BigDecimal amount;
    private long timestamp;
    private TransactionSource sourceSystem = TransactionSource.DEFAULT;

}
