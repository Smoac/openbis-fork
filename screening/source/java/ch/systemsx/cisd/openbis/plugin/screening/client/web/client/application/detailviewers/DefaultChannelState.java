package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.List;

/**
 * Allows to get and set the channel which is chosen by default when well images are shown. Channel
 * 0 consists of all other channels merged.
 * 
 * @author Tomasz Pylak
 */
public class DefaultChannelState
{
    private String defaultChannel = null;

    public DefaultChannelState()
    {

    }

    public String getDefaultChannel(List<String> channelsNames)
    {
        if (defaultChannel == null)
        {
            defaultChannel = channelsNames.get(0);
        }
        return defaultChannel;
    }

    public void setDefaultChannel(String value)
    {
        this.defaultChannel = value;
    }
}