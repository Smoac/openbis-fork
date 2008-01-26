package ch.systemsx.cisd.cifex.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Bean that should be used instead of the {@link PropertyPlaceholderConfigurer} if you want to have access to the
 * resolved properties not obligatory from the Spring context. e.g. from JSP or so.
 * 
 * @author Christian Ribeaud
 */
public final class ExposablePropertyPaceholderConfigurer extends PropertyPlaceholderConfigurer
{

    private Map<String, String> resolvedProps;

    /** Returns the resolved properties as unmodifiable map. */
    public final Map<String, String> getResolvedProps()
    {
        return Collections.unmodifiableMap(resolvedProps);
    }

    //
    // PropertyPlaceholderConfigurer
    //

    @Override
    protected final void processProperties(final ConfigurableListableBeanFactory beanFactoryToProcess,
            final Properties props) throws BeansException
    {
        super.processProperties(beanFactoryToProcess, props);
        resolvedProps = new HashMap<String, String>();
        for (final Object key : props.keySet())
        {
            final String keyStr = key.toString();
            resolvedProps.put(keyStr, parseStringValue(props.getProperty(keyStr), props, new HashSet()));
        }
    }

}