package ch.ethz.sis.afsserver.server.shuffling;

import java.util.List;
import java.util.UUID;

import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.afs.manager.ILockListener;

public interface IShareIdLockManager
{

    boolean lock(List<Lock<UUID, String>> locks);

    boolean unlock(List<Lock<UUID, String>> locks);

    void addListener(ILockListener<UUID, String> listener);

}
