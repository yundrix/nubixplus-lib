package com.nubixplus.lib.stereotype;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

/**
 * Identificador de transaccion que se arrastra en el MDC para correlacionar los
 * logs de una misma peticion.
 */
@UtilityClass
public class TransactionId {

    public static final String TRANSACTION_ID = "transactionId";

    public String get() {
        return Optional.ofNullable(MDC.get(TRANSACTION_ID)).orElseGet(TransactionId::init);
    }

    public String init() {
        final String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID, transactionId);
        return transactionId;
    }

    public void clear() {
        MDC.remove(TRANSACTION_ID);
    }
}
