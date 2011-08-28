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
package org.apache.oozie.action.hadoop;

import org.apache.hadoop.security.UserGroupInformation;
import org.apache.oozie.action.hadoop.DoAs;

import java.security.PrivilegedExceptionAction;
import java.util.concurrent.Callable;

//TODO this class goes away when doing 20.100+ only

//TODO this class is for testing, but is here to allow selective compilation
public class KerberosDoAs extends DoAs {

    public Void call() throws Exception {
        final Callable<Void> callable = getCallable();
        UserGroupInformation ugi = UserGroupInformation.createProxyUser(getUser(), UserGroupInformation.getLoginUser());
        ugi.doAs(new PrivilegedExceptionAction<Void>() {
            public Void run() throws Exception {
                callable.call();
                return null;
            }
        });
        return null;
    }
}
