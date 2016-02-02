/* Copyright (C) 2016 synapticpath.com - All Rights Reserved

 This file is part of Pi-Secure.

    Pi-Secure is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Pi-Secure is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Pi-Secure.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.synapticpath.raspberry;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests free-marker functionality that the project can use for multi-language support. 
 * @author jmarvan@synapticpath.com
 *
 */
public class FreeMarkerTest {

	public void test() throws Exception {
		//Configuration conf = new Configuration(Configuration.VERSION_2_3_22);
		
		
        Map root = new HashMap<>();
        root.put("user", "Big Joe");
        root.put("config", null);
        Map latest = new HashMap();
        root.put("latestProduct", latest);
        latest.put("url", "products/greenmouse.html");
        latest.put("name", "green mouse");

        //cfg.getTemplate("test.ftl");
        /* Get the template (uses cache internally) */
        //Template temp = new Template("name", "What is the name of the user ${user}? ${resources['scenarioOverview.answerOption.filter.no']}", conf);

        /* Merge data-model with template */
        Writer out = new OutputStreamWriter(System.out);
        
        //temp.process(root, out);
	}
}
