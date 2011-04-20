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

package org.apache.tuscany.sca.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;

/**
 * Main class for Tuscany. Just looking at what it means to read config from a directory structure. 
 *  
 */
public class Tuscany {

    public static void main(String[] args) throws Exception {
        String domainName = args[0];
        String nodeName = args[1];
        
        // find the domain directory
        File currentDirectory = new File(".");
        File domainDirectory = findDirectory(currentDirectory, domainName);
        System.out.println("Domain: " + domainDirectory.getPath());
        
        // find a sub directory that ends in nodeName
        File nodeDirectory = findDirectory(currentDirectory, nodeName);
        System.out.println("Node: " + nodeDirectory.getPath());
        
        // start a node with the node configuration
/* don't know how to start the node using the following        
        TuscanyRuntime tuscanyRuntime = TuscanyRuntime.newInstance();
        Node node = tuscanyRuntime.createNodeFromXML(nodeDirectory.getPath() + 
                                                     File.separator + 
                                                     "node.xml");
*/
        NodeFactory nodeFactory = NodeFactory.newInstance();
        
        URL nodeConfigURL = nodeDirectory.toURI().resolve("node.xml").toURL();
        Node node = nodeFactory.createNode(nodeConfigURL);
        
        node.start();
        
    }
    
    /**
     * Just walks down the tree (depth first) looking for a directory ending in the  
     * name. 
     */
    private static File findDirectory(File currentDirectory, String name){
        File directory = null;
        
        if (currentDirectory.getPath().endsWith(name)){
            directory = currentDirectory;
        } else {
            File[] subDirectories = currentDirectory.listFiles(new DirectoryFilter());
            for (File aDirectory : subDirectories) {
                directory = findDirectory(aDirectory, name);
                
                if (directory != null){
                    break;
                }
            }
        }
        
        return directory;
    }
    
    private static class DirectoryFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            if(new File(dir, name).isDirectory()) {
                return true;
            }
            
            return false;
        }
    }
}