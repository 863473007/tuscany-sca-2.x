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

package org.apache.tuscany.sca.host.webapp;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.host.http.ServletMappingException;

/**
 * ServletHost implementation for use in a webapp environment.
 * 
 * FIXME: using a static singleton seems a big hack but how should it be shared?
 * Need some way for TuscanyServlet to pull it out.
 */
public class WebAppServletHost implements ServletHost {
    private static final Logger logger = Logger.getLogger(WebAppServletHost.class.getName());

    private static final String SCA_DOMAIN_ATTRIBUTE = "org.apache.tuscany.sca.SCADomain";

    private static final WebAppServletHost instance = new WebAppServletHost();

    private Map<String, Servlet> servlets;
    private SCADomain scaDomain;
    private String contextPath = "/";
    private int defaultPortNumber = 8080;

    private WebAppServletHost() {
        servlets = new HashMap<String, Servlet>();
    }

    public void setDefaultPort(int port) {
        defaultPortNumber = port;
    }

    public int getDefaultPort() {
        return defaultPortNumber;
    }

    public void addServletMapping(String suri, Servlet servlet) throws ServletMappingException {
        URI pathURI = URI.create(suri);

        // Make sure that the path starts with a /
        suri = pathURI.getPath();
        if (!suri.startsWith("/")) {
            suri = '/' + suri;
        }

        if (!suri.startsWith(contextPath)) {
            suri = contextPath + suri;
        }

        // In a webapp just use the given path and ignore the host and port
        // as they are fixed by the Web container
        servlets.put(suri, servlet);

        logger.info("Added Servlet mapping: " + suri);
    }

    public Servlet removeServletMapping(String suri) throws ServletMappingException {
        URI pathURI = URI.create(suri);

        // Make sure that the path starts with a /
        suri = pathURI.getPath();
        if (!suri.startsWith("/")) {
            suri = '/' + suri;
        }

        if (!suri.startsWith(contextPath)) {
            suri = contextPath + suri;
        }

        // In a webapp just use the given path and ignore the host and port
        // as they are fixed by the Web container
        return servlets.remove(suri);
    }

    public Servlet getServletMapping(String suri) throws ServletMappingException {
        if (!suri.startsWith("/")) {
            suri = '/' + suri;
        }

        if (!suri.startsWith(contextPath)) {
            suri = contextPath + suri;
        }

        // Get the servlet mapped to the given path
        Servlet servlet = servlets.get(suri);
        return servlet;
    }

    public URL getURLMapping(String suri) throws ServletMappingException {
        URI uri = URI.create(suri);

        // Get the URI scheme and port
        String scheme = uri.getScheme();
        if (scheme == null) {
            scheme = "http";
        }
        int portNumber = uri.getPort();
        if (portNumber == -1) {
            portNumber = defaultPortNumber;
        }

        // Get the host
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "localhost";
        }

        // Construct the URL
        String path = uri.getPath();
        if (!path.startsWith("/")) {
            path = '/' + path;
        }

        if (!path.startsWith(contextPath)) {
            path = contextPath + path;
        }

        URL url;
        try {
            url = new URL(scheme, host, portNumber, path);
        } catch (MalformedURLException e) {
            throw new ServletMappingException(e);
        }
        return url;
    }

    public RequestDispatcher getRequestDispatcher(String suri) throws ServletMappingException {

        // Make sure that the path starts with a /
        if (!suri.startsWith("/")) {
            suri = '/' + suri;
        }

        suri = contextPath + suri;

        // Get the servlet mapped to the given path
        Servlet servlet = servlets.get(suri);
        if (servlet != null) {
            return new WebAppRequestDispatcher(suri, servlet);
        }

        for (Map.Entry<String, Servlet> entry : servlets.entrySet()) {
            String servletPath = entry.getKey();
            if (servletPath.endsWith("*")) {
                servletPath = servletPath.substring(0, servletPath.length() - 1);
                if (suri.startsWith(servletPath)) {
                    return new WebAppRequestDispatcher(entry.getKey(), entry.getValue());
                } else {
                    if ((suri + "/").startsWith(servletPath)) {
                        return new WebAppRequestDispatcher(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        // No servlet found
        return null;
    }

    public static WebAppServletHost getInstance() {
        return instance;
    }

    void init(ServletConfig config) throws ServletException {

        initContextPath(config);

        // Create an SCA domain object
        ServletContext servletContext = config.getServletContext();
        String domainURI = "http://localhost/" + contextPath;
        String contributionRoot = null;
        try {
            URL rootURL = servletContext.getResource("/");
            if (rootURL.getProtocol().equals("jndi")) {
                //this is tomcat case, we should use getRealPath
                File warRootFile = new File(servletContext.getRealPath("/"));
                contributionRoot = warRootFile.toURL().toString();
            } else {
                //this is jetty case
                contributionRoot = rootURL.toString();
            }
        } catch (MalformedURLException mf) {
            //ignore, pass null
        }
        scaDomain = SCADomain.newInstance(domainURI, contributionRoot);

        // Store the SCA domain in the servlet context
        servletContext.setAttribute(SCA_DOMAIN_ATTRIBUTE, scaDomain);

        // Initialize the registered servlets
        for (Servlet servlet : servlets.values()) {
            servlet.init(config);
        }
    }

    /**
     * Initializes the contextPath
     * The 2.5 Servlet API has a getter for this, for pre 2.5 servlet
     * containers use an init parameter.
     */
    @SuppressWarnings("unchecked")
    public void initContextPath(ServletConfig config) {
        ServletContext context = config.getServletContext();
        int version = context.getMajorVersion() * 100 + context.getMinorVersion();

        Method m;
        try {
            m = context.getClass().getMethod("getContextPath", new Class[] {});
            try {
                contextPath = (String)m.invoke(context, new Object[] {});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("'contextPath' init parameter must be set for pre-2.5 servlet container");
        }
        
        // Fall back for containers using old Servlet APIs
        if (contextPath == null) {
        	contextPath = config.getInitParameter("contextPath");
        }
        
        // contextPath == null => throw proper exception
        if (contextPath == null) {
        	throw new IllegalArgumentException("Could not retrieve webapp contextPath either from servletContext or init Parameter");
        }
        
        logger.info("initContextPath: " + contextPath);
    }

    void destroy() {

        // Destroy the registered servlets
        for (Servlet servlet : servlets.values()) {
            servlet.destroy();
        }

        // Close the SCA domain
        if (scaDomain != null) {
            scaDomain.close();
        }
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String path) {
        //        if (!contextPath.equals(path)) {
        //            throw new IllegalArgumentException("invalid context path for webapp, existing context path: " + contextPath + " new contextPath: " + path);
        //        }
    }

    /**
     * TODO: How context paths work is still up in the air so for now
     *    this hacks in a path that gets some samples working
     *    can't use setContextPath as NodeImpl calls that later
     */
    public void setContextPath2(String path) {
        this.contextPath = path;
    }
}
