package ch.ethz.sis.transaction;

import java.util.List;
import java.util.UUID;

public interface ITransactionParticipant
{

    String getParticipantId();

    /**
     * Used in:
     * - coordinator flow
     * - participant interactive flow
     */
    void beginTransaction(UUID transactionId, String sessionToken, String interactiveSessionKey, String transactionCoordinatorKey);

    /**
     * Used in:
     * - coordinator flow
     * - participant interactive flow
     */
    <T> T executeOperation(UUID transactionId, String sessionToken, String interactiveSessionKey, String operationName, Object[] operationArguments);

    /**
     * Used in:
     * - coordinator flow
     */
    void prepareTransaction(UUID transactionId, String sessionToken, String interactiveSessionKey, String transactionCoordinatorKey);

    /**
     * Used in:
     * - coordinator flow
     * - participant interactive flow
     */
    void commitTransaction(UUID transactionId, String sessionToken, String interactiveSessionKey);

    /**
     * Used in:
     * - coordinator recovery flow
     */
    void commitTransaction(UUID transactionId, String transactionCoordinatorKey);

    /**
     * Used in:
     * - coordinator flow
     * - participant interactive flow
     */
    void rollbackTransaction(UUID transactionId, String sessionToken, String interactiveSessionKey);

    /**
     * Used in:
     * - coordinator recovery flow
     */
    void rollbackTransaction(UUID transactionId, String transactionCoordinatorKey);

    /**
     * Used in:
     * - coordinator recovery flow
     */
    List<UUID> getTransactions(String transactionCoordinatorKey);

}
