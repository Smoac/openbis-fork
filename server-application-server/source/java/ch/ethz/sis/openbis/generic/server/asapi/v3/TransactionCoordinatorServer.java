package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.openbis.common.api.server.AbstractApiServiceExporter;

@Controller
public class TransactionCoordinatorServer extends AbstractApiServiceExporter
{

    @Autowired
    private ITransactionCoordinator transactionCoordinator;

    @Override
    public void afterPropertiesSet()
    {
        establishService(ITransactionCoordinator.class, transactionCoordinator, ITransactionCoordinator.SERVICE_NAME,
                ITransactionCoordinator.SERVICE_URL);
        super.afterPropertiesSet();
    }

    @RequestMapping(
            { ITransactionCoordinator.SERVICE_URL, "/openbis" + ITransactionCoordinator.SERVICE_URL,
                    "/openbis/openbis" + ITransactionCoordinator.SERVICE_URL })
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        super.handleRequest(request, response);
    }
}
