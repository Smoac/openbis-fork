package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.queue;

public interface ConsumerTask
{
    String getTaskName();
    void executeTask();
}
