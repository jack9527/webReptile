package org.webharvest.definition;

import org.webharvest.exception.ConfigurationException;
import org.webharvest.exception.ErrMsg;

import java.util.Set;
import java.util.TreeSet;
import java.util.StringTokenizer;
import java.util.Iterator;

/**
 * @author: Vladimir Nikic
 * Date: May 24, 2007
 */
public class ElementInfo {

    private String name;
    private Class definitionClass;
    private String validTags;
    private String validAtts;

    private Set tagsSet = new TreeSet();
    private Set requiredTagsSet = new TreeSet();
    private Set attsSet = new TreeSet();
    private Set requiredAttsSet = new TreeSet();

    private boolean allTagsAllowed;

    public ElementInfo(String name, Class definitionClass, String validTags, String validAtts) {
        this.name = name;
        this.definitionClass = definitionClass;
        this.validTags = validTags;
        this.validAtts = validAtts;

        this.allTagsAllowed = validTags == null;
        
        if (validTags != null) {
            StringTokenizer tokenizer = new StringTokenizer(validTags, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().toLowerCase();
                if ( token.startsWith("!") ) {
                    token = token.substring(1);
                    this.requiredTagsSet.add(token);
                }
                this.tagsSet.add(token);
            }
        }

        if (validAtts != null) {
            StringTokenizer tokenizer = new StringTokenizer(validAtts, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().toLowerCase();
                if ( token.startsWith("!") ) {
                    token = token.substring(1);
                    this.requiredAttsSet.add(token);
                }
                this.attsSet.add(token);
            }
        }
    }

    /**
     * @param onlyRequiredAtts
     * @return Template with allowed attributes. 
     */
    public String getTemplate(boolean onlyRequiredAtts) {
        StringBuffer result = new StringBuffer("<" + this.name);
        
        Set atts = onlyRequiredAtts ? this.requiredAttsSet : this.attsSet;

        Iterator iterator = atts.iterator();
        while (iterator.hasNext()) {
            String att = (String) iterator.next();
            result.append(" " + att + "=\"\"");
        }

        // if no valid subtags
        if ( this.validTags != null && "".equals(this.validTags.trim()) ) {
            result.append("/>");
        } else {
            result.append("></" + name + ">");
        }

        return result.toString();
    }

    public Class getDefinitionClass() {
        return definitionClass;
    }

    public String getName() {
        return name;
    }

    public Set getTagsSet() {
        return tagsSet;
    }

    public Set getAttsSet() {
        return attsSet;
    }

    public Set getRequiredAttsSet() {
        return requiredAttsSet;
    }

    public Set getRequiredTagsSet() {
        return requiredTagsSet;
    }

    public boolean areAllTagsAllowed() {
        return allTagsAllowed;
    }
    
}