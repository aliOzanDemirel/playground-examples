package transaction;

import transaction.controller.request.TransactionRequest;

public class RequestBodies {

    private RequestBodies() {
    }

    public static final TransactionRequest REQ_VALID =
            new TransactionRequest("1993.45", "2018-10-06T09:25:37.774Z");

    public static final TransactionRequest REQ_AMOUNT_INVALID =
            new TransactionRequest("98not.valid12", "2018-10-06T00:13:14.459Z");

    public static final TransactionRequest REQ_TIMESTAMP_INVALID_FORMAT =
            new TransactionRequest("13.47", "2018-10-06T01:14:14.Z");

    public static final TransactionRequest REQ_TIMESTAMP_INVALID_DATE =
            new TransactionRequest("883.02", "2018-13-40T25:63:14Z");

    public static final TransactionRequest REQ_TIMESTAMP_OLD_DATE =
            new TransactionRequest("3.0", "2017-10-01T09:15:14.662Z");

    public static final TransactionRequest REQ_INVALID_JSON =
            new TransactionRequest("", "timestamp");
}
