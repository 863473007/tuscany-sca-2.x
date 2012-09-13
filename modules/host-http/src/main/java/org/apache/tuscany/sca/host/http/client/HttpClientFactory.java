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

package org.apache.tuscany.sca.host.http.client;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.LifeCycleListener;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;

/**
 * @version $Rev$ $Date$
 */
public class HttpClientFactory implements LifeCycleListener {

    private HttpClient httpClient;

    public static HttpClientFactory getInstance(ExtensionPointRegistry registry) {
        UtilityExtensionPoint utilities = registry.getExtensionPoint(UtilityExtensionPoint.class);
        return utilities.getUtility(HttpClientFactory.class);
    }

    public HttpClient createHttpClient() {
        HttpParams defaultParameters = new BasicHttpParams();

        HttpProtocolParams.setContentCharset(defaultParameters, "UTF-8");
        HttpConnectionParams.setConnectionTimeout(defaultParameters, 60000);
        HttpConnectionParams.setSoTimeout(defaultParameters, 60000);

        // See https://issues.apache.org/jira/browse/HTTPCLIENT-1138
        SchemeRegistry supportedSchemes = null;
        try {
            supportedSchemes = SchemeRegistryFactory.createSystemDefault();
        } catch (SSLInitializationException e) {
            // Fall back to the default as some systems don't have all properties configured correctly, such as the keyStorePassword
            supportedSchemes = SchemeRegistryFactory.createDefault();
        }

        // FIXME: By pass host name verification
        SSLSocketFactory socketFactory = (SSLSocketFactory)supportedSchemes.getScheme("https").getSchemeSocketFactory();
        socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        PoolingClientConnectionManager connectionManager =
            new PoolingClientConnectionManager(supportedSchemes);

        connectionManager.setDefaultMaxPerRoute(256);
        connectionManager.setMaxTotal(1024);
        
        return new DefaultHttpClient(connectionManager, defaultParameters);
    }

    @Override
    public void start() {
        if (httpClient == null) {
            httpClient = createHttpClient();
        }
    }

    @Override
    public void stop() {
        if (httpClient != null) {
            httpClient.getConnectionManager().shutdown();
            httpClient = null;
        }
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
