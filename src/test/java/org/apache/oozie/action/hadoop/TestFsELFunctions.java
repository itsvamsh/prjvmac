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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.WorkflowActionBean;
import org.apache.oozie.DagELFunctions;
import org.apache.oozie.WorkflowJobBean;
import org.apache.oozie.workflow.lite.EndNodeDef;
import org.apache.oozie.workflow.lite.LiteWorkflowApp;
import org.apache.oozie.workflow.lite.LiteWorkflowInstance;
import org.apache.oozie.workflow.lite.StartNodeDef;
import org.apache.oozie.service.ELService;
import org.apache.oozie.service.Services;
import org.apache.oozie.test.XFsTestCase;
import org.apache.oozie.util.ELEvaluator;
import org.apache.oozie.util.XConfiguration;

public class TestFsELFunctions extends XFsTestCase {

    public void testFunctions() throws Exception {
        Services services = new Services();
        services.init();

        String file1 = new Path(getFsTestCaseDir(), "file1").toString();
        String file2 = new Path(getFsTestCaseDir(), "file2").toString();
        String dir = new Path(getFsTestCaseDir(), "dir").toString();
        Configuration protoConf = new Configuration();
        protoConf.set(OozieClient.USER_NAME, getTestUser());
        protoConf.set(OozieClient.GROUP_NAME, "group");
        protoConf.set("hadoop.job.ugi", getTestUser() + "," + "group");
        injectKerberosInfo(protoConf);

        FileSystem fs = getFileSystem();
        fs.mkdirs(new Path(dir));
        fs.create(new Path(file1)).close();
        OutputStream os = fs.create(new Path(dir, "a"));
        byte[] arr = new byte[1];
        os.write(arr);
        os.close();
        os = fs.create(new Path(dir, "b"));
        arr = new byte[2];
        os.write(arr);
        os.close();

        Configuration conf = new XConfiguration();
        conf.set(OozieClient.APP_PATH, "appPath");
        conf.set(OozieClient.USER_NAME, getTestUser());
        conf.set(OozieClient.GROUP_NAME, "group");
        injectKerberosInfo(protoConf);
        conf.set("test.dir", getTestCaseDir());
        conf.set("file1", file1);
        conf.set("file2", file2);
        conf.set("file3", "${file2}");
        conf.set("dir", dir);

        LiteWorkflowApp def =
                new LiteWorkflowApp("name", "<workflow-app/>", new StartNodeDef("end")).addNode(new EndNodeDef("end"));
        LiteWorkflowInstance job = new LiteWorkflowInstance(def, conf, "wfId");

        WorkflowJobBean wf = new WorkflowJobBean();
        wf.setId(job.getId());
        wf.setAppName("name");
        wf.setAppPath("appPath");
        wf.setUser(getTestUser());
        wf.setGroup("group");
        wf.setWorkflowInstance(job);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        protoConf.writeXml(baos);
        wf.setProtoActionConf(baos.toString());

        WorkflowActionBean action = new WorkflowActionBean();
        action.setId("actionId");
        action.setName("actionName");

        ELEvaluator eval = Services.get().get(ELService.class).createEvaluator("workflow");
        DagELFunctions.configureEvaluator(eval, wf, action);

        assertEquals(true, (boolean) eval.evaluate("${fs:exists(wf:conf('file1'))}", Boolean.class));
        assertEquals(false, (boolean) eval.evaluate("${fs:exists(wf:conf('file2'))}", Boolean.class));
        assertEquals(true, (boolean) eval.evaluate("${fs:exists(wf:conf('dir'))}", Boolean.class));
        assertEquals(false, (boolean) eval.evaluate("${fs:isDir(wf:conf('file1'))}", Boolean.class));
        assertEquals(0, (int) eval.evaluate("${fs:fileSize(wf:conf('file1'))}", Integer.class));
        assertEquals(-1, (int) eval.evaluate("${fs:fileSize(wf:conf('file2'))}", Integer.class));
        assertEquals(3, (int) eval.evaluate("${fs:dirSize(wf:conf('dir'))}", Integer.class));
        assertEquals(-1, (int) eval.evaluate("${fs:blockSize(wf:conf('file2'))}", Integer.class));
        assertTrue(eval.evaluate("${fs:blockSize(wf:conf('file1'))}", Integer.class) > 0);
    }

}
