package com.ibm.infosvr.restclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * The "label" asset type in IGC is one of the few that does not inherent the common characteristics of MainObject.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Label extends Reference {

    public ArrayList<Reference> _context = new ArrayList<Reference>();

    public String name;
    public String description;
    public ReferenceList labeled_assets;

}
