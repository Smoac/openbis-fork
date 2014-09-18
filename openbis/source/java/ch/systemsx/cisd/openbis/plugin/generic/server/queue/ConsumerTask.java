package ch.systemsx.cisd.openbis.plugin.generic.server.queue;

public interface ConsumerTask
{
    String getTaskName();
    void executeTask();
}
