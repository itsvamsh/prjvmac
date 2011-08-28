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
package org.apache.oozie.util;

import org.apache.oozie.test.XTestCase;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class TestELConstantsFunctions extends XTestCase {

    public void testTrim() {
        assertEquals("", ELConstantsFunctions.trim(null));
        assertEquals("a", ELConstantsFunctions.trim(" a "));
    }

    public void testConcat() {
        assertEquals("a", ELConstantsFunctions.concat("a", null));
        assertEquals("b", ELConstantsFunctions.concat(null, "b"));
        assertEquals("ab", ELConstantsFunctions.concat("a", "b"));
        assertEquals("", ELConstantsFunctions.concat(null, null));
    }

    public void testTimestamp() throws Exception {
        String s = ELConstantsFunctions.timestamp();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertNotNull(sdf.parse(s));
    }

    public void testUrlEncode() {
        assertEquals("+", ELConstantsFunctions.urlEncode(" "));
        assertEquals("%25", ELConstantsFunctions.urlEncode("%"));
    }
}
