package ch.ethz.sis.openbis.generic.server.transaction;

import ch.systemsx.cisd.common.api.IRpcService;

public interface ITransactionParticipantService extends ITransactionParticipant, IRpcService
{

    String SERVICE_NAME = "transaction-participant";

    String SERVICE_URL = "/rmi-" + SERVICE_NAME;

    String PARTICIPANT_ID = "application-server-participant-id";

    String PARTICIPANT_ID_2 = "application-server-participant-id-2";

}
