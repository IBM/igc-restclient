/* SPDX-License-Identifier: Apache-2.0 */
package com.ibm.infosvr.restclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Paging extends ObjectPrinter {

    protected Integer numTotal;
    protected String next;
    protected String previous;
    protected Integer pageSize;
    protected Integer end;
    protected Integer begin;

    public Paging() {
        this.numTotal = 0;
        this.next = null;
        this.previous = null;
        this.pageSize = 0;
        this.end = 0;
        this.begin = 0;
    }

    /**
     * Creates a new "full" Paging object (without any previous or next pages)
     *
     * @param numTotal - total number of objects that this "page" represents containing
     */
    public Paging(Integer numTotal) {
        this();
        this.numTotal = numTotal;
        this.pageSize = numTotal;
        this.end = numTotal;
    }

    @JsonProperty("numTotal")
    public Integer getNumTotal() { return this.numTotal; }
    public void setNumTotal(Integer numTotal) { this.numTotal = numTotal; }

    @JsonProperty("next")
    public String getNextPageURL() { return this.next; }
    public void setNextPageURL(String next) { this.next = next; }

    @JsonProperty("previous")
    public String getPreviousPageURL() { return this.previous; }
    public void setPreviousPageURL(String previous) { this.previous = previous; }

    @JsonProperty("pageSize")
    public Integer getPageSize() { return this.pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    @JsonProperty("end")
    public Integer getEndIndex() { return this.end; }
    public void setEndIndex(Integer end) { this.end = end; }

    @JsonProperty("begin")
    public Integer getBeginIndex() { return this.begin; }
    public void setBeginIndex(Integer begin) { this.begin = begin; }

    /**
     * Returns true iff there are more (unretrieved) pages for the paging that this object represents
     *
     * @return Boolean
     */
    public Boolean hasMore() {
        return (this.numTotal > this.end);
    }

}
