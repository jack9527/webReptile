/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime.web;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.webharvest.utils.CommonUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HTTP client functionality.
 */
public class HttpClientManager {

    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.1) Gecko/20060111 Firefox/1.5.0.1";

    static {
        // registers default handling for https 
        Protocol.registerProtocol("https", new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
    }

    private HttpClient client;

    private HttpInfo httpInfo;

    /**
     * Constructor.
     */
    public HttpClientManager() {
        client = new HttpClient();
        httpInfo = new HttpInfo(client);
        
        HttpClientParams clientParams = new HttpClientParams();
        clientParams.setBooleanParameter("http.protocol.allow-circular-redirects", true);
        client.setParams(clientParams);
    }

    public void setCookiePolicy(String cookiePolicy) {
        if ( "browser".equalsIgnoreCase(cookiePolicy) ) {
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        } else if ( "ignore".equalsIgnoreCase(cookiePolicy) ) {
            client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        } else if ( "netscape".equalsIgnoreCase(cookiePolicy) ) {
            client.getParams().setCookiePolicy(CookiePolicy.NETSCAPE);
        } else if ( "rfc_2109".equalsIgnoreCase(cookiePolicy) ) {
            client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        } else {
            client.getParams().setCookiePolicy(CookiePolicy.DEFAULT);
        }
    }

    /**
     * Defines HTTP proxy for the client with specified host and port
     * @param hostName
     * @param hostPort
     */
    public void setHttpProxy(String hostName, int hostPort) {
        client.getHostConfiguration().setProxyHost(new ProxyHost(hostName, hostPort));
    }

    /**
     * Defines HTTP proxy for the client with specified host
     * @param hostName
     */
    public void setHttpProxy(String hostName) {
    	client.getHostConfiguration().setProxyHost(new ProxyHost(hostName));
    }


    /**
     * Defines user credintials for the HTTP proxy server
     * @param username
     * @param password
     */
    public void setHttpProxyCredentials(String username, String password, String host, String domain) {
        Credentials credentials =
                ( host == null || domain == null || "".equals(host.trim()) || "".equals(domain.trim()) ) ?
                    new UsernamePasswordCredentials(username, password) :
                    new NTCredentials(username, password, host, domain);
        client.getState().setProxyCredentials( AuthScope.ANY, credentials);
    }
    
    public HttpResponseWrapper execute(
    		String methodType, 
    		String url, 
    		String charset,
    		String username, 
    		String password,
    		List params, 
    		Map headers ) {
        if ( !url.startsWith("http://") && !url.startsWith("https://") ) {
            url = "http://" + url;
        }

        url = CommonUtil.encodeUrl(url, charset);
        
        // if username and password are specified, define new credentials for authenticaton
        if ( username != null && password != null ) {
        	try {
				URL urlObj = new URL(url);
	            client.getState().setCredentials(
                    new AuthScope(urlObj.getHost(), urlObj.getPort()),
                    new UsernamePasswordCredentials(username, password)
                );
        	} catch (MalformedURLException e) {
				e.printStackTrace();
			}
        }
        
        HttpMethodBase method;
        if ( "post".equalsIgnoreCase(methodType) ) {
            method = createPostMethod(url, params);
        } else {
            method = createGetMethod(url, params, charset);
        }
        
        identifyAsDefaultBrowser(method);

        // define request headers, if any exist
        if (headers != null) {
            Iterator it = headers.keySet().iterator();
            while (it.hasNext()) {
                String headerName =  (String) it.next();
                String headerValue = (String) headers.get(headerName);
                method.addRequestHeader(new Header(headerName, headerValue));
            }
        }

        try {
            int statusCode = client.executeMethod(method);
            
            // if there is redirection, try to download redirection page
            if ((statusCode == HttpStatus.SC_MOVED_TEMPORARILY) ||
                (statusCode == HttpStatus.SC_MOVED_PERMANENTLY) ||
                (statusCode == HttpStatus.SC_SEE_OTHER) ||
                (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
                Header header = method.getResponseHeader("location");
                if (header != null) {
                    String newURI = header.getValue();
                    if ( newURI != null && !newURI.equals("") ) {
                    	newURI = CommonUtil.fullUrl(url, newURI);
                        method.releaseConnection();
                        method = new GetMethod(newURI);
                        identifyAsDefaultBrowser(method);
                        client.executeMethod(method);
                    }
                }
            }

            HttpResponseWrapper httpResponseWrapper = new HttpResponseWrapper(method);

            // updates HTTP info with response's details
            this.httpInfo.setResponse(httpResponseWrapper);

            return httpResponseWrapper;
        } catch (IOException e) {
            throw new org.webharvest.exception.HttpException("IO error during HTTP execution for URL: " + url, e);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Defines "User-Agent" HTTP header.
     * @param method
     */
    private void identifyAsDefaultBrowser(HttpMethodBase method) {
        method.addRequestHeader(new Header("User-Agent", DEFAULT_USER_AGENT));
    }

    private HttpMethodBase createPostMethod(String url, List params) {
        PostMethod method = new PostMethod(url);

        if (params != null) {
            NameValuePair[] paramArray = new NameValuePair[params.size()];
            Iterator it = params.iterator();
            int index = 0;
            while (it.hasNext()) {
                paramArray[index++] = (NameValuePair) it.next();
            }

            method.setRequestBody(paramArray);
        }

        return method;
    }

    private GetMethod createGetMethod(String url, List params, String charset) {
        if (params != null) {
            String urlParams = "";
            Iterator it = params.iterator();
            while (it.hasNext()) {
                NameValuePair pair = (NameValuePair) it.next();
                String value = pair.getValue();
                try {
                    urlParams += pair.getName() + "=" + URLEncoder.encode(value == null ? "" : value, charset) + "&";
                } catch (UnsupportedEncodingException e) {
                    throw new org.webharvest.exception.HttpException("Charset " + charset + " is not supported!", e);
                }
            }

            if (!"".equals(urlParams)) {
                if (url.indexOf("?") < 0) {
                    url += "?" + urlParams;
                } else if (url.endsWith("&")) {
                    url += urlParams;
                } else {
                    url += "&" + urlParams;
                }
            }
        }

        return new GetMethod(url);
    }

    public HttpClient getHttpClient() {
        return client;
    }

    public HttpInfo getHttpInfo() {
        return httpInfo;
    }
    
}