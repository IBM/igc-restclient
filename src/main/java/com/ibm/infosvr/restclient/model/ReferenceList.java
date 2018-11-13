package com.ibm.infosvr.restclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ibm.infosvr.restclient.IGCRestClient;

import java.util.ArrayList;

/**
 * Provides a standard class for any relationship in IGC, by including 'paging' details and 'items' array.
 *
 * Used in POJOs, this class can be defined as the type of any relationship attribute, eg.:
 *   public ReferenceList assigned_assets;
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReferenceList extends ObjectPrinter {

    public Paging paging = new Paging();
    public ArrayList<Reference> items = new ArrayList<Reference>();

    /**
     * Returns true iff there are more (unretrieved) pages for the relationships that this object represents
     *
     * @return Boolean
     */
    public Boolean hasMorePages() {
        return (this.paging.hasMore());
    }

    /**
     * Retrieve all pages of relationships that this object represents
     *
     * @param igcrest - the IGCRestClient connection to use to retrieve the relationships
     */
    public void getAllPages(IGCRestClient igcrest) {
        this.items = igcrest.getAllPages(this.items, this.paging);
        this.paging = new Paging(this.items.size());
    }

}
