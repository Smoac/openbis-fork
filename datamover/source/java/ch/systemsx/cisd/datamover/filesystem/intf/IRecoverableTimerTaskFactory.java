package ch.systemsx.cisd.datamover.filesystem.intf;
import java.util.TimerTask;

/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A factory for creating {@link TimerTask}s that, when run, perform a recovery operation on the data mover.
 *
 * @author Bernd Rinn
 */
public interface IRecoverableTimerTaskFactory
{

    public TimerTask createRecoverableTimerTask();

}
