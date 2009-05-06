package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

/**
 * Interface to delegate export and refresh actions.
 */
public interface IBrowserGridActionInvoker
{
    void export();

    void refresh();
}