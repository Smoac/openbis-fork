package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.ethz.sis.openbis.generic.server.transaction.ITransactionCoordinatorService;
import ch.systemsx.cisd.openbis.common.api.server.AbstractApiServiceExporter;

@Controller
public class TransactionCoordinatorServer extends AbstractApiServiceExporter
{

    @Autowired
    private ITransactionCoordinatorService transactionCoordinatorService;

    @Override
    public void afterPropertiesSet()
    {
        establishService(ITransactionCoordinatorService.class, transactionCoordinatorService, ITransactionCoordinatorService.SERVICE_NAME,
                ITransactionCoordinatorService.SERVICE_URL);
        super.afterPropertiesSet();
    }

    @RequestMapping(
            { ITransactionCoordinatorService.SERVICE_URL, "/openbis" + ITransactionCoordinatorService.SERVICE_URL,
                    "/openbis/openbis" + ITransactionCoordinatorService.SERVICE_URL })
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        super.handleRequest(request, response);
    }
}
