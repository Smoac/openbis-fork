package ch.ethz.sis.afsserver.server.shuffling;

import java.util.List;
import java.util.UUID;

import ch.ethz.sis.afs.dto.Lock;

public interface IShareIdLockManager
{

    void lock(List<Lock<UUID, String>> locks);

    void unlock(List<Lock<UUID, String>> locks);

}
