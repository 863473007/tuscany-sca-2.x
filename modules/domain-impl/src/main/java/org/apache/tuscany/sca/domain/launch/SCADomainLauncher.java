/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.tuscany.sca.domain.launch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.tuscany.sca.domain.SCADomain;
import org.apache.tuscany.sca.domain.SCADomainFactory;

public class SCADomainLauncher {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Tuscany Domain Controller starting...");

        SCADomain domain = null;
        try {
            SCADomainFactory domainFactory = SCADomainFactory.newInstance();
            domain = domainFactory.createSCADomain("http://localhost:9999");
            
        } catch (Exception e) {
            System.err.println("Exception starting domain controller");
            e.printStackTrace();
            System.exit(0);
        }
        
        System.out.println("Domain controller ready...");
        System.out.println("Press enter to shutdown");
        try {
            System.in.read();
        } catch (IOException e) {
        }
        
        try {
            domain.destroy();
        } catch (Exception e) {
            System.err.println("Exception stopping domain controller");
            e.printStackTrace();
        }
        
        System.exit(0);
    }
    
    private static final String PING_HEADER =
        "GET / HTTP/1.0\n" + "Host: localhost\n"
            + "Content-Type: text/xml\n"
            + "Connection: close\n"
            + "Content-Length: ";
    private static final String PING_CONTENT = "";
    private static final String PING =
        PING_HEADER + PING_CONTENT.getBytes().length + "\n\n" + PING_CONTENT;

    /**
     * Ping the domain controller at http://localhost:9999
     * 
     * @return true if the domain is there, false otherwise
     * @throws IOException
     */
    public static boolean pingDomain() throws IOException {
        try {
            Socket client = new Socket("localhost", 9999);
            OutputStream os = client.getOutputStream();
            os.write(PING.getBytes());
            os.flush();
            read(client);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String read(Socket socket) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

}
