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
package org.webharvest.definition;

/**
 * Definition of HTML to XML rensformation task.
 */
public class HtmlToXmlDef extends BaseElementDef {

    private String outputType;
    private String advancedXmlEscape;
    private String useCdataForScriptAndStyle;
    private String translateSpecialEntities;
    private String recognizeUnicodeChars;
    private String omitUnknownTags;
    private String treatUnknownTagsAsContent;
    private String omitDeprecatedTags;
    private String treatDeprecatedTagsAsContent;
    private String omitComments;
    private String omitHtmlEnvelope;
    private String allowMultiWordAttributes;
    private String allowHtmlInsideAttributes;
    private String namespacesAware;
    private String prunetags;

    public HtmlToXmlDef(XmlNode xmlNode) {
        super(xmlNode);

        this.outputType = (String) xmlNode.get("outputtype");
        this.advancedXmlEscape = (String) xmlNode.get("advancedxmlescape");
        this.useCdataForScriptAndStyle = (String) xmlNode.get("usecdata");
        this.translateSpecialEntities = (String) xmlNode.get("specialentities");
        this.recognizeUnicodeChars = (String) xmlNode.get("unicodechars");
        this.omitUnknownTags = (String) xmlNode.get("omitunknowntags");
        this.treatUnknownTagsAsContent = (String) xmlNode.get("treatunknowntagsascontent");
        this.omitDeprecatedTags = (String) xmlNode.get("omitdeprtags");
        this.treatDeprecatedTagsAsContent = (String) xmlNode.get("treatdeprtagsascontent");
        this.omitComments = (String) xmlNode.get("omitcomments");
        this.omitHtmlEnvelope = (String) xmlNode.get("omithtmlenvelope");
        this.allowMultiWordAttributes = (String) xmlNode.get("allowmultiwordattributes");
        this.allowHtmlInsideAttributes = (String) xmlNode.get("allowhtmlinsideattributes");
        this.namespacesAware = (String) xmlNode.get("namespacesaware");
        this.prunetags = (String) xmlNode.get("prunetags");
    }

    public String getShortElementName() {
        return "html-to-xml";
    }

    public String getOutputType() {
        return outputType;
    }

    public String getAdvancedXmlEscape() {
        return advancedXmlEscape;
    }

    public String getUseCdataForScriptAndStyle() {
        return useCdataForScriptAndStyle;
    }

    public String getTranslateSpecialEntities() {
        return translateSpecialEntities;
    }

    public String getRecognizeUnicodeChars() {
        return recognizeUnicodeChars;
    }

    public String getOmitUnknownTags() {
        return omitUnknownTags;
    }

    public String getTreatUnknownTagsAsContent() {
        return treatUnknownTagsAsContent;
    }

    public String getOmitDeprecatedTags() {
        return omitDeprecatedTags;
    }

    public String getTreatDeprecatedTagsAsContent() {
        return treatDeprecatedTagsAsContent;
    }

    public String getOmitComments() {
        return omitComments;
    }

    public String getOmitHtmlEnvelope() {
        return omitHtmlEnvelope;
    }

    public String getAllowMultiWordAttributes() {
        return allowMultiWordAttributes;
    }

    public String getAllowHtmlInsideAttributes() {
        return allowHtmlInsideAttributes;
    }

    public String getNamespacesAware() {
        return namespacesAware;
    }

    public String getPrunetags() {
        return prunetags;
    }

}