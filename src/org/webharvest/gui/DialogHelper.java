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
import java.awt.*;
import java.io.File;

/**
 * Class that ease work with common dialogs.
 * All methods are static in order to be used easier from everywhere. The only
 * requirement is that parent component have to be initialized before any use.  
 *
 * @author: Vladimir Nikic
 * Date: May 17, 2007
 */
public class DialogHelper {

    // parent component for all the dialogs
    private static Component parent;

    // default file chooser
    private final static JFileChooser fileChooser = new JFileChooser();

    public static synchronized void init(Component parent) {
        DialogHelper.parent = parent;
        fileChooser.setFileFilter( new XmlFileFilter() );
        fileChooser.setMultiSelectionEnabled(true);
    }

    /**
     * Splits string in multiple lines if it is too long. 
     * @param msg
     * @return Splitted string.
     */
    private static String prepareMsg(String msg) {
        final int maxLength = 80;
        StringBuffer result = new StringBuffer("");
        int lineLength = 0;
        if (msg != null) {
            for (int i = 0; i < msg.length(); i++) {
                char ch = msg.charAt(i);
                if ( (ch == '\n') || (ch == ' ' && lineLength > maxLength) ) {
                    result.append('\n');
                    lineLength = 0;
                } else {
                    result.append(ch);
                    lineLength++;
                }
            }
        }

        return result.toString();
    }

    /**
     * Displays dialog with specified error message.
     * @param msg
     */
    public static void showErrorMessage(String msg) {
        JOptionPane.showMessageDialog(parent, prepareMsg(msg), "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays dialog with specified warning message.
     * @param msg
     */
    public static void showWarningMessage(String msg) {
        JOptionPane.showMessageDialog(parent, prepareMsg(msg), "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Displays dialog with specified information.
     * @param msg
     */
    public static void showInfoMessage(String msg) {
        JOptionPane.showMessageDialog(parent, prepareMsg(msg), "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays confirmation dialog with YES and NO options in the form of question.
     * @param questionMsg
     * @return True if user has chosen YES, false otherwise.
     */
    public static boolean showYesNoConfirmQuestion(String questionMsg) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                prepareMsg(questionMsg),
                "Question",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Displays confirmation dialog with YES and NO options in the form of warning.
     * @param warningMsg
     * @return True if user has chosen YES, false otherwise.
     */
    public static boolean showYesNoConfirmWarning(String warningMsg) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                prepareMsg(warningMsg),
                "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Displays confirmation dialog with YES, NO and CANCEL options in the form of question.
     * @param questionMsg
     * @return Result inherited from JOptionPane.showConfirmDialog.
     */
    public static int showYesNoCancelConfirmQuestion(String questionMsg) {
       return JOptionPane.showConfirmDialog(
                parent,
                prepareMsg(questionMsg),
                "Question",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    /**
     * Displays confirmation dialog with YES, NO and CANCEL options in the form of warning.
     * @param warningMsg
     * @return Result inherited from JOptionPane.showConfirmDialog.
     */
    public static int showYesNoCancelConfirmWarning(String warningMsg) {
       return JOptionPane.showConfirmDialog(
                parent,
                prepareMsg(warningMsg),
                "Warning",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
    }

    /**
     * @return Default file chooser instance for the application.
     */
    public static JFileChooser getFileChooser() {
        return fileChooser;
    }

}