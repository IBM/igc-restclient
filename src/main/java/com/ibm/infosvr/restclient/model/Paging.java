package com.ibm.infosvr.restclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Paging extends ObjectPrinter {

    public Integer numTotal;
    public String next;
    public String previous;
    public Integer pageSize;
    public Integer end;
    public Integer begin;

    public Paging() {
        this.numTotal = 0;
        this.next = null;
        this.previous = null;
        this.pageSize = 0;
        this.end = 0;
        this.begin = 0;
    }

    public Paging(Integer numTotal) {
        this();
        this.numTotal = numTotal;
        this.pageSize = numTotal;
        this.end = numTotal;
    }

    /**
     * Returns true iff there are more (unretrieved) pages for the paging that this object represents
     *
     * @return Boolean
     */
    public Boolean hasMore() {
        return (this.numTotal > this.end);
    }

    /**
     * Modifies the page size to use within this paging object (batching parameter)
     *
     * Note that this should only be used prior to paging through any results, as IGC
     * will return strange subsets if used after paging through results has already begun
     *
     * @param size - the new page (batch) size to use
     */
    public void modifyPageSize(Integer size) {
        if (this.pageSize != size && this.next != null && this.next.contains("&pageSize=")) {
            this.next = this.next.replace("&pageSize=" + this.pageSize, "&pageSize=" + size);
            this.pageSize = size;
        }
    }

}
