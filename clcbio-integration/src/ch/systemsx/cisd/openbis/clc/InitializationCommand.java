package ch.systemsx.cisd.openbis.clc;

import com.clcbio.api.base.algorithm.Algo;
import com.clcbio.api.base.session.ApplicationContext;
import com.clcbio.server.api.command.ServerCommand;
import com.clcbio.server.command.algo.ServerAlgoCommand;

/**
 * Dummy AlgoCommand that starts initialization of openBIS persistence model.
 * 
 * @author anttil
 */
public class InitializationCommand extends ServerAlgoCommand
{
    public static final String PLUGIN_GROUP = "free";

    private Initialization algo;

    @Override
    public void init(ApplicationContext context)
    {
        super.init(context);
        algo = new Initialization(context);
    }

    @Override
    public ServerCommand createInstance()
    {
        return new InitializationCommand();
    }

    @Override
    public Algo getAlgorithm()
    {
        return algo;
    }

    @Override
    public double getVersion()
    {
        return algo.getVersion();
    }

    @Override
    public String getClassKey()
    {
        return algo.getClassKey();
    }

}
