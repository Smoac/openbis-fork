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
public class TransactionManagerServer extends AbstractApiServiceExporter
{

    @Autowired
    private ITransactionManager transactionManager;

    @Override
    public void afterPropertiesSet()
    {
        establishService(ITransactionManager.class, transactionManager, ITransactionManager.SERVICE_NAME,
                ITransactionManager.SERVICE_URL);
        super.afterPropertiesSet();
    }

    @RequestMapping(
            { ITransactionManager.SERVICE_URL, "/openbis" + ITransactionManager.SERVICE_URL,
                    "/openbis/openbis" + ITransactionManager.SERVICE_URL })
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        super.handleRequest(request, response);
    }
}
