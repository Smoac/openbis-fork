package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;


/**
 * Allows to get and set the channel which is chosen by default when images are shown in a specific context.
 *  
 * @author Piotr Buczek
 */
public interface IDefaultChannelState
{
    public String tryGetDefaultChannel();

    public void setDefaultChannel(String value);
}