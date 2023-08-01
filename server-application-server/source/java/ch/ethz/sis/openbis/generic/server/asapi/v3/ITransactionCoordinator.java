package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.UUID;

import ch.systemsx.cisd.common.api.IRpcService;

public interface ITransactionCoordinator
{

    void beginTransaction(UUID transactionId, String sessionToken, String interactiveSessionKey);

    Object executeOperation(UUID transactionId, String sessionToken, String interactiveSessionKey, String participantId, String operationName,
            Object[] operationArguments);

    void commitTransaction(UUID transactionId, String sessionToken, String interactiveSessionKey);

    void rollbackTransaction(UUID transactionId, String sessionToken, String interactiveSessionKey);

}
