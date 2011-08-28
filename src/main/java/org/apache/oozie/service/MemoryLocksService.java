/**
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. See accompanying LICENSE file.
 */
package org.apache.oozie.service;

import org.apache.oozie.util.Instrumentable;
import org.apache.oozie.util.Instrumentation;
import org.apache.oozie.util.MemoryLocks;

public class MemoryLocksService implements Service, Instrumentable {
    private static final String INSTRUMENTATION_GROUP = "locks";
    private MemoryLocks locks;

    /**
     * Initialize the memory locks service
     *
     * @param services services instance.
     */
    @Override
    public void init(Services services) {
        locks = new MemoryLocks();
    }

    /**
     * Destroy the memory locks service.
     */
    @Override
    public void destroy() {
        locks = null;
    }

    /**
     * Return the public interface for the memory locks services
     *
     * @return {@link MemoryLocksService}.
     */
    @Override
    public Class<? extends Service> getInterface() {
        return MemoryLocksService.class;
    }

    /**
     * Instruments the memory locks service.
     *
     * @param instr instance to instrument the memory locks service to.
     */
    public void instrument(Instrumentation instr) {
        final MemoryLocks finalLocks = this.locks;
        instr.addVariable(INSTRUMENTATION_GROUP, "locks", new Instrumentation.Variable<Long>() {
            public Long getValue() {
                return (long) finalLocks.size();
            }
        });
    }

    /**
     * Obtain a READ lock for a source.
     *
     * @param resource resource name.
     * @param wait time out in milliseconds to wait for the lock, -1 means no timeout and 0 no wait.
     * @return the lock token for the resource, or <code>null</code> if the lock could not be obtained.
     * @throws InterruptedException thrown if the thread was interrupted while waiting.
     */
    public MemoryLocks.LockToken getReadLock(String resource, long wait) throws InterruptedException {
        return locks.getReadLock(resource, wait);
    }

    /**
     * Obtain a WRITE lock for a source.
     *
     * @param resource resource name.
     * @param wait time out in milliseconds to wait for the lock, -1 means no timeout and 0 no wait.
     * @return the lock token for the resource, or <code>null</code> if the lock could not be obtained.
     * @throws InterruptedException thrown if the thread was interrupted while waiting.
     */
    public MemoryLocks.LockToken getWriteLock(String resource, long wait) throws InterruptedException {
        return locks.getWriteLock(resource, wait);
    }

}
