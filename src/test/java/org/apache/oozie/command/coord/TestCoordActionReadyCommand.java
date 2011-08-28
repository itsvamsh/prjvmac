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
package org.apache.oozie.command.coord;

import java.util.Date;

import org.apache.oozie.CoordinatorActionBean;
import org.apache.oozie.CoordinatorJobBean;
import org.apache.oozie.client.CoordinatorAction;
import org.apache.oozie.client.CoordinatorJob;
import org.apache.oozie.client.CoordinatorAction.Status;
import org.apache.oozie.client.CoordinatorJob.Execution;
import org.apache.oozie.command.CommandException;
import org.apache.oozie.service.Services;
import org.apache.oozie.service.StoreService;
import org.apache.oozie.store.CoordinatorStore;
import org.apache.oozie.store.StoreException;
import org.apache.oozie.test.XTestCase;
import org.apache.oozie.util.DateUtils;

public class TestCoordActionReadyCommand extends XTestCase {
    private Services services;

    protected void setUp() throws Exception {
        super.setUp();
        services = new Services();
        services.init();
    }

    protected void tearDown() throws Exception {
        services.destroy();
        super.tearDown();
    }

    public void testActionReadyCommand() throws StoreException,
            CommandException {
        String jobId = "0000000-" + new Date().getTime()
                + "-testActionReadyCommand-C";
        CoordinatorStore store = Services.get().get(StoreService.class)
                .getStore(CoordinatorStore.class);
        try {
            addRecordToJobTable(jobId, store);
            addRecordToActionTable(jobId, 1, store);
        }
        finally {
            store.closeTrx();
        }
        new CoordActionReadyCommand(jobId).call();
        checkCoordAction(jobId + "@1");
    }

    private void addRecordToActionTable(String jobId, int actionNum,
                                        CoordinatorStore store) throws StoreException {
        // CoordinatorStore store = new CoordinatorStore(false);
        CoordinatorActionBean action = new CoordinatorActionBean();
        action.setJobId(jobId);
        action.setId(jobId + "@" + actionNum);
        action.setActionNumber(actionNum);
        action.setNominalTime(new Date());
        action.setLastModifiedTime(new Date());
        action.setStatus(Status.READY);
        // action.setActionXml("");
        store.beginTrx();
        store.insertCoordinatorAction(action);
        store.commitTrx();
    }

    private void addRecordToJobTable(String jobId, CoordinatorStore store)
            throws StoreException {
        // CoordinatorStore store = new CoordinatorStore(false);
        CoordinatorJobBean coordJob = new CoordinatorJobBean();
        coordJob.setId(jobId);
        coordJob.setAppName("testApp");
        coordJob.setAppPath("testAppPath");
        coordJob.setStatus(CoordinatorJob.Status.RUNNING);
        coordJob.setCreatedTime(new Date()); // TODO: Do we need that?
        coordJob.setLastModifiedTime(new Date());
        coordJob.setUser("testUser");
        coordJob.setGroup("testGroup");
        coordJob.setAuthToken("notoken");

        String confStr = "<configuration></configuration>";
        coordJob.setConf(confStr);
        String appXml = "<coordinator-app xmlns='uri:oozie:coordinator:0.1' name='NAME' frequency=\"1\" start='2009-02-01T01:00Z' end='2009-02-03T23:59Z' timezone='UTC' freq_timeunit='DAY' end_of_duration='NONE'>";
        appXml += "<controls>";
        appXml += "<timeout>10</timeout>";
        appXml += "<concurrency>2</concurrency>";
        appXml += "<execution>LIFO</execution>";
        appXml += "</controls>";
        appXml += "<input-events>";
        appXml += "<data-in name='A' dataset='a'>";
        appXml += "<dataset name='a' frequency='7' initial-instance='2009-02-01T01:00Z' timezone='UTC' freq_timeunit='DAY' end_of_duration='NONE'>";
        appXml += "<uri-template>file:///tmp/coord/workflows/${YEAR}/${DAY}</uri-template>";
        appXml += "</dataset>";
        appXml += "<instance>${coord:latest(0)}</instance>";
        appXml += "</data-in>";
        appXml += "</input-events>";
        appXml += "<output-events>";
        appXml += "<data-out name='LOCAL_A' dataset='local_a'>";
        appXml += "<dataset name='local_a' frequency='7' initial-instance='2009-02-01T01:00Z' timezone='UTC' freq_timeunit='DAY' end_of_duration='NONE'>";
        appXml += "<uri-template>file:///tmp/coord/workflows/${YEAR}/${DAY}</uri-template>";
        appXml += "</dataset>";
        appXml += "<instance>${coord:current(-1)}</instance>";
        appXml += "</data-out>";
        appXml += "</output-events>";
        appXml += "<action>";
        appXml += "<workflow>";
        appXml += "<app-path>hdfs:///tmp/workflows/</app-path>";
        appXml += "<configuration>";
        appXml += "<property>";
        appXml += "<name>inputA</name>";
        appXml += "<value>${coord:dataIn('A')}</value>";
        appXml += "</property>";
        appXml += "<property>";
        appXml += "<name>inputB</name>";
        appXml += "<value>${coord:dataOut('LOCAL_A')}</value>";
        appXml += "</property>";
        appXml += "</configuration>";
        appXml += "</workflow>";
        appXml += "</action>";
        appXml += "</coordinator-app>";
        coordJob.setJobXml(appXml);
        coordJob.setLastActionNumber(0);
        coordJob.setFrequency(1);
        coordJob.setExecution(Execution.FIFO);
        coordJob.setConcurrency(1);
        try {
            coordJob.setEndTime(DateUtils.parseDateUTC("2009-02-03T23:59Z"));
            coordJob.setStartTime(DateUtils.parseDateUTC("2009-02-01T23:59Z"));
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // store.closeTrx();
            fail("Could not set Date/time");
        }

        try {
            store.beginTrx();
            store.insertCoordinatorJob(coordJob);
            store.commitTrx();
        }
        catch (StoreException se) {
            se.printStackTrace();
            store.rollbackTrx();
            fail("Unable to insert the test job record to table");
            throw se;
        }
    }

    private void checkCoordAction(String actionId) throws StoreException {
        CoordinatorStore store = Services.get().get(StoreService.class)
                .getStore(CoordinatorStore.class);
        try {
            store.beginTrx();
            CoordinatorActionBean action = store.getCoordinatorAction(actionId,
                                                                      true);
            if (action.getStatus() != CoordinatorAction.Status.SUBMITTED) {
                fail("CoordActionReadyCommand didn't work because the status for action id"
                        + actionId + " is :" + action.getStatus());
            }
            store.commitTrx();
        }
        catch (StoreException se) {
            fail("Action ID " + actionId + " was not stored properly in db");
        }
        finally {
            store.closeTrx();
        }
    }

}
