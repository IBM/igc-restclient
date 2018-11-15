/* SPDX-License-Identifier: Apache-2.0 */
package com.ibm.infosvr.restclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ibm.infosvr.restclient.IGCRestClient;

import java.lang.reflect.Field;
import java.sql.Ref;
import java.util.ArrayList;

/**
 * Provides a standard class for any relationship in IGC, by including 'paging' details and 'items' array.
 *
 * Used in POJOs, this class can be defined as the type of any relationship attribute, eg.:
 *   public ReferenceList assigned_assets;
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReferenceList extends ObjectPrinter {

    protected Paging paging = new Paging();
    protected ArrayList<Reference> items = new ArrayList<Reference>();

    @JsonProperty("paging")
    public Paging getPaging() { return this.paging; }
    public void setPaging(Paging paging) { this.paging = paging; }

    @JsonProperty("items")
    public ArrayList<Reference> getItems() { return this.items; }
    public void setItems(ArrayList<Reference> items) { this.items = items; }

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
