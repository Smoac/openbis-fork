package ch.systemsx.cisd.openbis.clc;

import com.clcbio.api.base.algorithm.Algo;
import com.clcbio.api.base.algorithm.AlgoException;
import com.clcbio.api.base.algorithm.CallableExecutor;
import com.clcbio.api.base.algorithm.OutputHandler;
import com.clcbio.api.base.persistence.model.PersistenceModel;
import com.clcbio.api.base.persistence.model.PersistenceModelManager;
import com.clcbio.api.base.session.ApplicationContext;
import com.clcbio.api.free.framework.persistence.model.PersistenceModelDescriptionBean;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.impl.OpenbisServiceFacade;

/**
 * Dummy algorithm which does no calculation, but initializes openBIS persistence model.
 * 
 * @author anttil
 */
public class Initialization extends Algo
{

    public Initialization(ApplicationContext applicationContext)
    {
        super(applicationContext);
        PersistenceModelManager manager = PersistenceModelManager.getInstance();
        IOpenbisServiceFacade openbis = OpenbisServiceFacade.tryCreate("selenium", "password", "http://localhost:10000", 1000000L);
        PersistenceModel model = new OpenBISPersistenceModel("openBIS", new SpaceProvider(openbis));
        manager.insertPersistenceModels(new PersistenceModel[] { model });
        PersistenceModelDescriptionBean modelDescription = new PersistenceModelDescriptionBean(model);
        manager.mountPersistenceModels(modelDescription, "openBIS");
        System.out.println("openBIS integration plugin enabled");
    }

    @Override
    public void calculate(OutputHandler handler, CallableExecutor arg1)
            throws AlgoException, InterruptedException
    {
    }

    @Override
    public String getName()
    {
        return "openBIS integration plugin (not runnable)";
    }

    @Override
    public String getClassKey()
    {
        return "openBIS";
    }

    @Override
    public double getVersion()
    {
        return 1.0;
    }
}
