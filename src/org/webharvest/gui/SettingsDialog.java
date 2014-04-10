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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class SettingsDialog extends JDialog implements ChangeListener {

    private class MyTextField extends JTextField {
        public MyTextField() {
            super();
        }

        public MyTextField(String text) {
            super(text);
        }

        public Dimension getPreferredSize() {
            return new Dimension(250, 20);
        }
    }

    // Ide instance where this dialog belongs.
    private Ide ide;

    // settiongs fields
    private JTextField workingPathField;
    private JTextField proxyServerField;
    private JTextField proxyPortField;
    private JTextField proxyUsernameField;
    private JTextField proxyPasswordField;
    private JTextField ntlmHostField;
    private JTextField ntlmDomainField;
    private JCheckBox proxyEnabledCheckBox;
    private JCheckBox proxyAuthEnabledCheckBox;
    private JCheckBox ntlmEnabledCheckBox;

    private JLabel proxyUsernameLabel;
    private JLabel proxyPasswordLabel;
    private JLabel proxyPortLabel;
    private JLabel proxyServerLabel;
    private JLabel ntlmHostLabel;
    private JLabel ntlmDomainLabel;

    private JCheckBox showHierarchyByDefaultCheckBox;
    private JCheckBox showLogByDefaultCheckBox;
    private JCheckBox showLineNumbersByDefaultCheckBox;
    private JCheckBox dynamicConfigLocateCheckBox;

    private final JFileChooser pathChooser = new JFileChooser();

    public SettingsDialog(Ide ide) throws HeadlessException {
        super(ide, "Settings", true);
        this.ide = ide;
        this.setResizable(false);

        pathChooser.setFileFilter( new FileFilter() {
            public boolean accept(File f) {
                return f.exists() && f.isDirectory();
            }
            public String getDescription() {
                return "All directories";
            }
        });
        pathChooser.setMultiSelectionEnabled(false);
        pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        createGUI();
    }

    private void createGUI() {
        Container contentPane = this.getContentPane();

        JPanel generalPanel = new JPanel(new GridBagLayout());
        generalPanel.setBorder( new EmptyBorder(4, 4, 4, 4) );

        contentPane.setLayout( new BorderLayout() );

        JTabbedPane tabbedPane = new JTabbedPane();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 5, 2, 5);

        workingPathField = new MyTextField();

        proxyServerField = new MyTextField();
        proxyPortField = new MyTextField();
        proxyUsernameField = new MyTextField();
        proxyPasswordField = new MyTextField();
        ntlmHostField = new MyTextField();
        ntlmDomainField = new MyTextField();

        proxyEnabledCheckBox = new JCheckBox("Proxy server enabled");
        proxyEnabledCheckBox.addChangeListener(this);
        proxyAuthEnabledCheckBox = new JCheckBox("Proxy authentication enabled");
        proxyAuthEnabledCheckBox.addChangeListener(this);
        ntlmEnabledCheckBox = new JCheckBox("Use NTLM authentication scheme");
        ntlmEnabledCheckBox.addChangeListener(this);

        constraints.gridx = 0;
        constraints.gridy = 0;
        generalPanel.add( new JLabel("Output path"), constraints );

        constraints.gridx = 1;
        constraints.gridy = 0;
        JPanel pathPanel = new JPanel( new FlowLayout(FlowLayout.LEFT, 0, 0) );
        pathPanel.add(workingPathField);
        JButton chooseDirButton = new JButton(" ... ") {
            public Dimension getPreferredSize() {
                return new Dimension(20, 20);
            }
        };
        chooseDirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = pathChooser.showOpenDialog(SettingsDialog.this);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = pathChooser.getSelectedFile();
                    if (selectedDir != null) {
                        workingPathField.setText( selectedDir.getAbsolutePath() );
                    }
                }
            }
        });
        pathPanel.add(chooseDirButton);
        generalPanel.add(pathPanel, constraints );

        constraints.gridx = 0;
        constraints.gridy = 1;
        generalPanel.add(proxyEnabledCheckBox , constraints );

        constraints.gridx = 0;
        constraints.gridy = 2;
        proxyServerLabel = new JLabel("Proxy server");
        generalPanel.add(proxyServerLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 2;
        generalPanel.add(proxyServerField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 3;
        proxyPortLabel = new JLabel("Proxy port (blank is default)");
        generalPanel.add(proxyPortLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 3;
        generalPanel.add(proxyPortField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 4;
        generalPanel.add(proxyAuthEnabledCheckBox , constraints );

        constraints.gridx = 0;
        constraints.gridy = 5;
        proxyUsernameLabel = new JLabel("Proxy username");
        generalPanel.add(proxyUsernameLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 5;
        generalPanel.add(proxyUsernameField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 6;
        proxyPasswordLabel = new JLabel("Proxy password");
        generalPanel.add(proxyPasswordLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 6;
        generalPanel.add(proxyPasswordField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 7;
        generalPanel.add(ntlmEnabledCheckBox , constraints );

        constraints.gridx = 0;
        constraints.gridy = 8;
        ntlmHostLabel = new JLabel("NT host");
        generalPanel.add(ntlmHostLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 8;
        generalPanel.add(ntlmHostField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 9;
        ntlmDomainLabel = new JLabel("NT domain");
        generalPanel.add(ntlmDomainLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 9;
        generalPanel.add(ntlmDomainField, constraints );

        JPanel buttonPanel = new JPanel( new FlowLayout(FlowLayout.CENTER) );

        JButton okButton = new JButton("  Ok  ");
        buttonPanel.add(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                define();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPanel.add(cancelButton);

        JPanel viewPanel = new JPanel();
        viewPanel.setBorder( new EmptyBorder(4, 4, 4, 4) );
        viewPanel.setLayout( new BoxLayout(viewPanel, BoxLayout.PAGE_AXIS) );
        this.showHierarchyByDefaultCheckBox = new JCheckBox("Show hierarchy panel by default");
        this.showLogByDefaultCheckBox = new JCheckBox("Show log panel by default");
        this.showLineNumbersByDefaultCheckBox = new JCheckBox("Show line numbers by default");
        this.dynamicConfigLocateCheckBox = new JCheckBox("Dynamically locate processors in runtime");

        viewPanel.add(this.showHierarchyByDefaultCheckBox);
        viewPanel.add(this.showLogByDefaultCheckBox);
        viewPanel.add(this.showLineNumbersByDefaultCheckBox);
        viewPanel.add(this.dynamicConfigLocateCheckBox);

        tabbedPane.addTab("General", null, generalPanel, null);
        tabbedPane.addTab("View", null, viewPanel, null);

        contentPane.add(tabbedPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        updateControls();

        this.pack();
    }

    private void fillValues() {
        Settings settings = ide.getSettings();

        workingPathField.setText( settings.getWorkingPath() );
        proxyServerField.setText( settings.getProxyServer() );
        proxyPortField.setText( settings.getProxyPort() > 0 ? "" + settings.getProxyPort() : "" );
        proxyUsernameField.setText( settings.getProxyUserename() );
        proxyPasswordField.setText( settings.getProxyPassword() );
        proxyEnabledCheckBox.setSelected( settings.isProxyEnabled() );
        proxyAuthEnabledCheckBox.setSelected( settings.isProxyAuthEnabled() );

        ntlmEnabledCheckBox.setSelected( settings.isNtlmAuthEnabled() );
        ntlmHostField.setText( settings.getNtlmHost() );
        ntlmDomainField.setText( settings.getNtlmDomain() );

        showHierarchyByDefaultCheckBox.setSelected( settings.isShowHierarchyByDefault() );
        showLogByDefaultCheckBox.setSelected( settings.isShowLogByDefault() );
        showLineNumbersByDefaultCheckBox.setSelected( settings.isShowLineNumbersByDefault() );
        dynamicConfigLocateCheckBox.setSelected( settings.isDynamicConfigLocate() );
    }

    public void setVisible(boolean b) {
        if (b) {
            fillValues();
        }
        super.setVisible(b);
    }

    private void define() {
        Settings settings = this.ide.getSettings();

        settings.setWorkingPath( this.workingPathField.getText() );
        settings.setProxyServer( this.proxyServerField.getText() );

        int port = -1;
        try {
            port = Integer.parseInt( this.proxyPortField.getText() );
        } catch (NumberFormatException e) {
        }
        settings.setProxyPort(port);

        settings.setProxyUserename( this.proxyUsernameField.getText() );
        settings.setProxyPassword( this.proxyPasswordField.getText() );

        settings.setProxyEnabled( this.proxyEnabledCheckBox.isSelected() );
        settings.setProxyAuthEnabled( this.proxyAuthEnabledCheckBox.isSelected() );

        settings.setNtlmAuthEnabled( this.ntlmEnabledCheckBox.isSelected() );
        settings.setNtlmHost( this.ntlmHostField.getText() );
        settings.setNtlmDomain( this.ntlmDomainField.getText() );

        settings.setShowHierarchyByDefault(this.showHierarchyByDefaultCheckBox.isSelected());
        settings.setShowLogByDefault(this.showLogByDefaultCheckBox.isSelected());
        settings.setShowLineNumbersByDefault(this.showLineNumbersByDefaultCheckBox.isSelected());
        settings.setDynamicConfigLocate(this.dynamicConfigLocateCheckBox.isSelected());

        try {
            settings.writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showErrorMessage("Error saving programs settings: " + e.getMessage());
        }

        updateControls();

        setVisible(false);
    }

    /**
     * Enable/disable controls depending on setting values.
     */
    private void updateControls() {
        boolean isProxyEnabled = this.proxyEnabledCheckBox.isSelected();
        boolean isProxyAuthEnabled = this.proxyAuthEnabledCheckBox.isSelected();
        boolean isNtlmAuthEnabled = this.ntlmEnabledCheckBox.isSelected();

        this.proxyServerLabel.setEnabled(isProxyEnabled);
        this.proxyServerField.setEnabled(isProxyEnabled);
        this.proxyPortLabel.setEnabled(isProxyEnabled);
        this.proxyPortField.setEnabled(isProxyEnabled);

        this.proxyAuthEnabledCheckBox.setEnabled(isProxyEnabled);

        this.proxyUsernameLabel.setEnabled( isProxyEnabled && isProxyAuthEnabled );
        this.proxyUsernameField.setEnabled( isProxyEnabled && isProxyAuthEnabled );
        this.proxyPasswordLabel.setEnabled( isProxyEnabled && isProxyAuthEnabled );
        this.proxyPasswordField.setEnabled( isProxyEnabled && isProxyAuthEnabled );

        this.ntlmEnabledCheckBox.setEnabled(isProxyEnabled && isProxyAuthEnabled);

        this.ntlmHostLabel.setEnabled( isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled );
        this.ntlmHostField.setEnabled( isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled );
        this.ntlmDomainLabel.setEnabled( isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled );
        this.ntlmDomainField.setEnabled( isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled );
    }

    public void stateChanged(ChangeEvent e) {
        updateControls();
    }

    protected JRootPane createRootPane() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                define();
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);


        return rootPane;
    }
    
}