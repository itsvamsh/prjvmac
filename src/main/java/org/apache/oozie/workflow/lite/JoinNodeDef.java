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
package org.apache.oozie.workflow.lite;

import org.apache.oozie.workflow.WorkflowException;
import org.apache.oozie.ErrorCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO javadoc
public class JoinNodeDef extends NodeDef {

    JoinNodeDef() {
    }

    public JoinNodeDef(String name, String transition) {
        super(name, null, JoinNodeHandler.class, Arrays.asList(transition));
    }

    public static class JoinNodeHandler extends NodeHandler {

        public void loopDetection(Context context) throws WorkflowException {
            String flag = getLoopFlag(context.getNodeDef().getName());
            if (context.getVar(flag) != null) {
                throw new WorkflowException(ErrorCode.E0709, context.getNodeDef().getName());
            }
            String parentExecutionPath = context.getParentExecutionPath(context.getExecutionPath());
            String forkCount = context.getVar(ForkNodeDef.FORK_COUNT_PREFIX + parentExecutionPath);
            if (forkCount == null) {
                throw new WorkflowException(ErrorCode.E0720, context.getNodeDef().getName());
            }
            int count = Integer.parseInt(forkCount) - 1;
            if (count == 0) {
                context.setVar(flag, "true");
            }
        }

        public boolean enter(Context context) throws WorkflowException {
            String parentExecutionPath = context.getParentExecutionPath(context.getExecutionPath());
            String forkCount = context.getVar(ForkNodeDef.FORK_COUNT_PREFIX + parentExecutionPath);
            if (forkCount == null) {
                throw new WorkflowException(ErrorCode.E0720, context.getNodeDef().getName());
            }
            int count = Integer.parseInt(forkCount) - 1;
            if (count > 0) {
                context.setVar(ForkNodeDef.FORK_COUNT_PREFIX + parentExecutionPath, "" + count);
                context.deleteExecutionPath();
            }
            else {
                context.setVar(ForkNodeDef.FORK_COUNT_PREFIX + parentExecutionPath, null);
            }
            return (count == 0);
        }

        public List<String> multiExit(Context context) {
            String parentExecutionPath = context.getParentExecutionPath(context.getExecutionPath());
            // NOW we delete..
            context.deleteExecutionPath();

            String transition = context.getNodeDef().getTransitions().get(0);
            String fullTransition = context.createFullTransition(parentExecutionPath, transition);
            List<String> transitions = new ArrayList<String>(1);
            transitions.add(fullTransition);
            return transitions;
        }

        public String exit(Context context) {
            throw new UnsupportedOperationException();
        }

        public void kill(Context context) {
        }

        public void fail(Context context) {
        }
    }

}
