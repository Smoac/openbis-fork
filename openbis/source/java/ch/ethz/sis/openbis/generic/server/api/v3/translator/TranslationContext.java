package ch.ethz.sis.openbis.generic.server.api.v3.translator;

import ch.ethz.sis.openbis.generic.server.api.v3.context.Context;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

public class TranslationContext extends Context
{
    private final TranslationCache translationCache;

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public TranslationContext(Session session, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        super(session);
        this.translationCache = new TranslationCache();
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    public TranslationCache getTranslationCache()
    {
        return translationCache;
    }

    public IManagedPropertyEvaluatorFactory getManagedPropertyEvaluatorFactory()
    {
        return managedPropertyEvaluatorFactory;
    }

}
