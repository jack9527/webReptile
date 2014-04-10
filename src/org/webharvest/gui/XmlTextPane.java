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

/**
 * @author: Vladimir Nikic
 * Date: Apr 20, 2007
 */

import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLDocument;
import org.bounce.text.xml.XMLStyleConstants;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.text.*;

public class XmlTextPane extends JEditorPane {

    /**
     * Action which occures on TAB key pressed inside the editor. It implements clever
     * block indenting - when selection contains more than one line of text, the whole
     * block is indented one tab place to the right. 
     */
    private class TabAction extends TextAction {
        public TabAction() {
            super(DefaultEditorKit.insertTabAction);
        }

        private void indentText(JTextComponent textComponent) throws BadLocationException {
            String selectedText = textComponent.getSelectedText();
            int newLineIndex = selectedText != null ? selectedText.indexOf('\n') : -1;


            if (newLineIndex >= 0) {
                int originalSelectionStart = textComponent.getSelectionStart();
                int selectionStart = originalSelectionStart;
                int selectionEnd = textComponent.getSelectionEnd();

                int lastNewLineBeforeSelection = textComponent.getText(0, selectionStart).lastIndexOf('\n');
                int begin = lastNewLineBeforeSelection >= 0 ? lastNewLineBeforeSelection : 0;
                int end = selectionEnd;

                String text = textComponent.getText(begin, end - begin);
                int len = text.length();
                StringBuffer out = new StringBuffer(len);
                if (lastNewLineBeforeSelection < 0) {
                    out.insert(0, '\t');
                    selectionStart++;
                    selectionEnd++;
                }
                for (int i = 0; i < len; i++) {
                    char ch = text.charAt(i);
                    out.append(ch);
                    if (ch == '\n' && i < len - 1) {
                        out.append("\t");
                        selectionEnd++;
                        if (begin + i < originalSelectionStart) {
                            selectionStart++;
                        }
                    }
                }

                textComponent.select(begin, end);
                textComponent.replaceSelection(out.toString());

                textComponent.select(selectionStart, selectionEnd);
            } else {
                textComponent.replaceSelection("\t");
            }
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if ( !textComponent.isEditable() || !textComponent.isEnabled() ) {
                return;
            }

            try {
                indentText(textComponent);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Action which occures on SHIFT-TAB key pressed inside the editor. It implements clever
     * block outdenting - when selection contains more than one line of text, the whole
     * block is outdented one tab place to the left. 
     */
    private class ShiftTabAction extends TextAction {
        public ShiftTabAction() {
            super("shift-tab-action");
        }

        private void outdentText(JTextComponent textComponent) throws BadLocationException {
            int tabSize = ((Integer)textComponent.getDocument().getProperty(PlainDocument.tabSizeAttribute)).intValue();

            String selectedText = textComponent.getSelectedText();
            int newLineIndex = selectedText != null ? selectedText.indexOf('\n') : -1;

            if (newLineIndex >= 0) {
                int originalSelectionStart = textComponent.getSelectionStart();
                int selectionStart = originalSelectionStart;
                int selectionEnd = textComponent.getSelectionEnd();

                int lastNewLineBeforeSelection = textComponent.getText(0, selectionStart).lastIndexOf('\n');
                int begin = lastNewLineBeforeSelection >= 0 ? lastNewLineBeforeSelection : 0;
                int end = selectionEnd;

                String text = textComponent.getText(begin, end - begin);
                if (lastNewLineBeforeSelection < 0) {
                    text = "\n" + text;
                }
                int len = text.length();
                StringBuffer out = new StringBuffer(len);
                for (int i = 0; i < len; i++) {
                    char ch = text.charAt(i);
                    out.append(ch);
                    if (ch == '\n' && i < len - 1) {
                        char next = text.charAt(i + 1);
                        int stripCount = 0;
                        if (next == '\t') {
                            stripCount = 1;
                        } else {
                            for ( ; stripCount < tabSize && i + 1 + stripCount < len; stripCount++ ) {
                                next = text.charAt(i + 1 + stripCount);
                                if ( next != ' ' && next != '\t' ) {
                                    break;
                                }
                            }
                        }

                        selectionEnd -= stripCount;
                        if (i + begin < originalSelectionStart - 1) {
                            selectionStart -= stripCount;
                        }
                        i += stripCount;
                    }
                }

                textComponent.select(begin, end);
                textComponent.replaceSelection(lastNewLineBeforeSelection < 0 ? out.toString().substring(1) : out.toString());

                textComponent.select(selectionStart, selectionEnd);
            }
        }

        public void actionPerformed(ActionEvent e){
            JTextComponent textComponent = getTextComponent(e);
            if ( !textComponent.isEditable() || !textComponent.isEnabled() ) {
                return;
            }

            try {
                outdentText(textComponent);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    private UndoManager undoManager = new UndoManager();

    private TabAction tabAction = new TabAction();
    private ShiftTabAction shiftTabAction = new ShiftTabAction();

    public XmlTextPane() {
        XMLEditorKit kit = new XMLEditorKit(true);

        kit.setLineWrappingEnabled(false);

        kit.setStyle( XMLStyleConstants.ELEMENT_NAME, new Color(128, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ELEMENT_VALUE, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ELEMENT_PREFIX, new Color(128, 0, 0), Font.PLAIN);

        kit.setStyle( XMLStyleConstants.ATTRIBUTE_NAME, new Color(255, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ATTRIBUTE_VALUE, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ATTRIBUTE_PREFIX, new Color(128, 0, 0), Font.PLAIN);

        kit.setStyle( XMLStyleConstants.NAMESPACE_NAME, new Color(102, 102, 102), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.NAMESPACE_VALUE, new Color(0, 51, 51), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.NAMESPACE_PREFIX, new Color(102, 102, 102), Font.PLAIN);

        kit.setStyle( XMLStyleConstants.ENTITY, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.COMMENT, new Color(153, 153, 153), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.CDATA, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.SPECIAL, new Color(0, 0, 0), Font.PLAIN);

//        kit.setStyle( XMLStyleConstants.ELEMENT_NAME, new Color( 0, 0, 128), Font.BOLD);
//        kit.setStyle( XMLStyleConstants.ELEMENT_VALUE, Color.black, Font.BOLD);
//        kit.setStyle( XMLStyleConstants.ELEMENT_PREFIX, new Color( 0, 0, 128), Font.PLAIN);
//
//        kit.setStyle( XMLStyleConstants.ATTRIBUTE_NAME, Color.blue, Font.BOLD);
//        kit.setStyle( XMLStyleConstants.ATTRIBUTE_VALUE, new Color( 0, 128, 0), Font.BOLD);
//        kit.setStyle( XMLStyleConstants.ATTRIBUTE_PREFIX, Color.blue, Font.PLAIN);
//
//        kit.setStyle( XMLStyleConstants.NAMESPACE_NAME, new Color( 102, 102, 102), Font.PLAIN);
//        kit.setStyle( XMLStyleConstants.NAMESPACE_VALUE, new Color( 0, 51, 51), Font.PLAIN);
//        kit.setStyle( XMLStyleConstants.NAMESPACE_PREFIX, new Color( 0, 102, 102), Font.PLAIN);
//
//        kit.setStyle( XMLStyleConstants.ENTITY, new Color( 102, 102, 102), Font.PLAIN);
//        kit.setStyle( XMLStyleConstants.COMMENT, Color.gray, Font.PLAIN);
//        kit.setStyle( XMLStyleConstants.CDATA, Color.black, Font.PLAIN);
//        kit.setStyle( XMLStyleConstants.SPECIAL, Color.black, Font.BOLD);


        this.setEditorKit(kit);

        this.setFont( new Font( "Monospaced", Font.PLAIN, 12));

        this.registerKeyboardAction(shiftTabAction, KeyStroke.getKeyStroke( KeyEvent.VK_TAB, ActionEvent.SHIFT_MASK), JComponent.WHEN_FOCUSED);
        this.getActionMap().put(tabAction.getValue(Action.NAME), tabAction);

    }

    public boolean getScrollableTracksViewportWidth() {
        //should not allow text to be wrapped
      return false;
    }

    public void undo() {
        if ( this.undoManager.canUndo() ) {
            this.undoManager.undo();
        }
    }

    public void redo() {
        if ( this.undoManager.canRedo() ) {
            this.undoManager.redo();
        }
    }

    public UndoableEditListener getUndoManager() {
        return undoManager;
    }

    /**
     * @return True if some text is selected, false otherwise
     */
    public boolean hasSelection() {
        String selectedText = this.getSelectedText();
        return selectedText != null && !"".equals(selectedText);
    }

}