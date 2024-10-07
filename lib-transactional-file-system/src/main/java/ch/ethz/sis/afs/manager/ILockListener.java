package ch.ethz.sis.afs.manager;

import java.util.List;

import ch.ethz.sis.afs.dto.Lock;

public interface ILockListener<O, E>
{

    void onLocksAdded(List<Lock<O, E>> locks);

    void onLocksRemoved(List<Lock<O, E>> locks);

}
