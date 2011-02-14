package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.List;

/**
 * Allows to get and set the channels chosen by default when images are shown in a specific context.
 * 
 * @author Piotr Buczek
 */
public interface IDefaultChannelState
{
    public List<String> tryGetDefaultChannels();

    public void setDefaultChannels(List<String> channels);
}