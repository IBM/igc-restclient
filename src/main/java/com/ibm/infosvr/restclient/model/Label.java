/* SPDX-License-Identifier: Apache-2.0 */
package com.ibm.infosvr.restclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * The "label" asset type in IGC is one of the few that does not inherent the common characteristics of MainObject.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Label extends Reference {

    protected ArrayList<Reference> _context = new ArrayList<Reference>();

    protected String name;
    protected String description;
    protected ReferenceList labeled_assets;

    @JsonProperty("_context")
    public ArrayList<Reference> getContext() { return this._context; }
    public void setContext(ArrayList<Reference> _context) { this._context = _context; }

    @JsonProperty("name")
    public String getTheName() { return this.name; }
    public void setTheName(String name) { this.name = name; }

    @JsonProperty("description")
    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    @JsonProperty("labeled_assets")
    public ReferenceList getLabeledAssets() { return this.labeled_assets; }
    public void setLabeledAssets(ReferenceList labeled_assets) { this.labeled_assets = labeled_assets; }

    public static final Boolean isLabel(Object obj) {
        return (obj.getClass() == Label.class);
    }

}
