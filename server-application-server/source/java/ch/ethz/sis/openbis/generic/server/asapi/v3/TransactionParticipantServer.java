package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.ethz.sis.openbis.generic.server.transaction.ITransactionParticipantService;
import ch.systemsx.cisd.openbis.common.api.server.AbstractApiServiceExporter;

@Controller
public class TransactionParticipantServer extends AbstractApiServiceExporter
{

    @Autowired
    private ITransactionParticipantService transactionParticipantService;

    @Override
    public void afterPropertiesSet()
    {
        establishService(ITransactionParticipantService.class, transactionParticipantService, ITransactionParticipantService.SERVICE_NAME,
                ITransactionParticipantService.SERVICE_URL);
        super.afterPropertiesSet();
    }

    @RequestMapping(
            { ITransactionParticipantService.SERVICE_URL, "/openbis" + ITransactionParticipantService.SERVICE_URL,
                    "/openbis/openbis" + ITransactionParticipantService.SERVICE_URL })
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        super.handleRequest(request, response);
    }
}
