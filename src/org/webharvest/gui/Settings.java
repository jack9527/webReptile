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
package org.webharvest.gui;

import java.io.*;

/**
 * @author: Vladimir Nikic
 * Date: Apr 27, 2007
 */
public class Settings implements Serializable {

    private static final String CONFIG_FILE_PATH = System.getProperty("java.io.tmpdir") + "/webharvest.config";

    private String workingPath = System.getProperty("java.io.tmpdir");
    private boolean isProxyEnabled;
    private String proxyServer;
    private int proxyPort = -1;
    private boolean isProxyAuthEnabled;
    private String proxyUserename;
    private String proxyPassword;
    private boolean isNtlmAuthEnabled;
    private String ntlmHost;
    private String ntlmDomain;

    private boolean isShowHierarchyByDefault = true;
    private boolean isShowLogByDefault = true;
    private boolean isShowLineNumbersByDefault = true;

    // specify if processors are located in source while configuration is running
    private boolean isDynamicConfigLocate = true;

    public Settings() {
        try {
            readFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showErrorMessage("Error while reading programs settings: " + e.getMessage());
        }
    }

    public boolean isProxyAuthEnabled() {
        return isProxyAuthEnabled;
    }

    public void setProxyAuthEnabled(boolean proxyAuthEnabled) {
        isProxyAuthEnabled = proxyAuthEnabled;
    }

    public boolean isProxyEnabled() {
        return isProxyEnabled;
    }

    public void setProxyEnabled(boolean proxyEnabled) {
        isProxyEnabled = proxyEnabled;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    public String getProxyUserename() {
        return proxyUserename;
    }

    public void setProxyUserename(String proxyUserename) {
        this.proxyUserename = proxyUserename;
    }

    public boolean isNtlmAuthEnabled() {
        return isNtlmAuthEnabled;
    }

    public void setNtlmAuthEnabled(boolean ntlmAuthEnabled) {
        isNtlmAuthEnabled = ntlmAuthEnabled;
    }

    public String getNtlmDomain() {
        return ntlmDomain;
    }

    public void setNtlmDomain(String ntlmDomain) {
        this.ntlmDomain = ntlmDomain;
    }

    public String getNtlmHost() {
        return ntlmHost;
    }

    public void setNtlmHost(String ntlmHost) {
        this.ntlmHost = ntlmHost;
    }

    public String getWorkingPath() {
        return workingPath;
    }

    public void setWorkingPath(String workingPath) {
        this.workingPath = workingPath;
    }

    public boolean isDynamicConfigLocate() {
        return this.isDynamicConfigLocate;
    }

    public void setDynamicConfigLocate(boolean dynamicConfigLocate) {
        isDynamicConfigLocate = dynamicConfigLocate;
    }

    public boolean isShowHierarchyByDefault() {
        return isShowHierarchyByDefault;
    }

    public void setShowHierarchyByDefault(boolean showHierarchyByDefault) {
        isShowHierarchyByDefault = showHierarchyByDefault;
    }

    public boolean isShowLogByDefault() {
        return isShowLogByDefault;
    }

    public void setShowLogByDefault(boolean showLogByDefault) {
        isShowLogByDefault = showLogByDefault;
    }

    public boolean isShowLineNumbersByDefault() {
        return isShowLineNumbersByDefault;
    }

    public void setShowLineNumbersByDefault(boolean showLineNumbersByDefault) {
        isShowLineNumbersByDefault = showLineNumbersByDefault;
    }

    private void writeString(ObjectOutputStream out, String s) throws IOException {
        if (s != null) {
            out.writeInt(s.getBytes().length);
            out.writeBytes(s);
        } else {
            out.writeInt(0);
        }
    }

    private String readString(ObjectInputStream in) throws IOException {
        byte[] bytes = new byte[in.readInt()];
        in.read(bytes);
        return new String(bytes);
    }

    /**
     * Serialization write.
     * @param out
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        writeString(out, workingPath);

        out.writeBoolean(isProxyEnabled);
        writeString(out, proxyServer);
        out.writeInt(proxyPort);
        out.writeBoolean(isProxyAuthEnabled);
        writeString(out, proxyUserename);
        writeString(out, proxyPassword);
        out.writeBoolean(isNtlmAuthEnabled);
        writeString(out, ntlmHost);
        writeString(out, ntlmDomain);

        out.writeBoolean(isShowHierarchyByDefault);
        out.writeBoolean(isShowLogByDefault);
        out.writeBoolean(isShowLineNumbersByDefault);
        out.writeBoolean(isDynamicConfigLocate);

    }

    /**
     * Serialization read.
     * @param in
     * @throws IOException
     */
    private void readObject(ObjectInputStream in) throws IOException {
        workingPath = readString(in);

        isProxyEnabled = in.readBoolean();
        proxyServer = readString(in);
        proxyPort = in.readInt();
        isProxyAuthEnabled = in.readBoolean();
        proxyUserename = readString(in);
        proxyPassword = readString(in);
        isNtlmAuthEnabled = in.readBoolean();
        ntlmHost = readString(in);
        ntlmDomain = readString(in);

        isShowHierarchyByDefault = in.readBoolean();
        isShowLogByDefault = in.readBoolean();
        isShowLineNumbersByDefault = in.readBoolean();
        isDynamicConfigLocate = in.readBoolean();
    }

    private void readFromFile() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        if ( configFile.exists() ) {
            FileInputStream fis = new FileInputStream(configFile);
	        ObjectInputStream ois = new ObjectInputStream(fis);
            readObject(ois);
        }
    }

    public void writeToFile() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        FileOutputStream fos = new FileOutputStream(configFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        writeObject(oos);
        oos.flush();
        fos.flush();
        oos.close();
        fos.close();
    }

}