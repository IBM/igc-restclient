/* SPDX-License-Identifier: Apache-2.0 */
package com.ibm.infosvr.restclient.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ibm.infosvr.restclient.IGCRestClient;
import com.ibm.infosvr.restclient.search.IGCSearch;
import com.ibm.infosvr.restclient.search.IGCSearchCondition;
import com.ibm.infosvr.restclient.search.IGCSearchConditionSet;

import java.util.ArrayList;
import java.util.Date;

/**
 * The supertype of the vast majority of IGC objects.
 *
 * Simply define a new POJO as extending this base class to inherit the attributes that are found
 * on virtually all IGC asset types.
 */
public abstract class MainObject extends Reference {

    @JsonIgnore private Identity identity = null;

    protected ArrayList<Reference> _context = new ArrayList<Reference>();

    protected String name = null;
    protected String short_description = null;
    protected String long_description = null;

    protected ReferenceList labels = null;
    protected ReferenceList stewards = null;
    protected ReferenceList assigned_to_terms = null;
    protected ReferenceList implements_rules = null;
    protected ReferenceList governed_by_rules = null;

    protected String created_by;
    protected Date created_on;
    protected String modified_by;
    protected Date modified_on;

    // TODO: add notes object reference

    @JsonProperty("_context")
    public ArrayList<Reference> getContext() { return this._context; }
    public void setContext(ArrayList<Reference> _context) { this._context = _context; }

    @JsonProperty("name")
    public String getTheName() { return this.name; }
    public void setTheName(String name) { this.name = name; }

    @JsonProperty("short_description")
    public String getShortDescription() { return this.short_description; }
    public void setShortDescription(String short_description) { this.short_description = short_description; }

    @JsonProperty("long_description")
    public String getLongDescription() { return this.long_description; }
    public void setLongDescription(String long_description) { this.long_description = long_description; }

    @JsonProperty("labels")
    public ReferenceList getLabels() { return this.labels; }
    public void setLabels(ReferenceList labels) { this.labels = labels; }

    @JsonProperty("stewards")
    public ReferenceList getStewards() { return this.stewards; }
    public void setStewards(ReferenceList stewards) { this.stewards = stewards; }

    @JsonProperty("assigned_to_terms")
    public ReferenceList getAssignedToTerms() { return this.assigned_to_terms; }
    public void setAssignedToTerms(ReferenceList assigned_to_terms) { this.assigned_to_terms = assigned_to_terms; }

    @JsonProperty("implements_rules")
    public ReferenceList getImplementsRules() { return this.implements_rules; }
    public void setImplementsRules(ReferenceList implements_rules) { this.implements_rules = implements_rules; }

    @JsonProperty("governed_by_rules")
    public ReferenceList getGovernedByRules() { return this.governed_by_rules; }
    public void setGovernedByRules(ReferenceList governed_by_rules) { this.governed_by_rules = governed_by_rules; }

    @JsonProperty("created_by")
    public String getCreatedBy() { return this.created_by; }
    public void setCreatedBy(String created_by) { this.created_by = created_by; }

    @JsonProperty("created_on")
    public Date getCreatedOn() { return this.created_on; }
    public void setCreatedOn(Date created_on) { this.created_on = created_on; }

    @JsonProperty("modified_by")
    public String getModifiedBy() { return this.modified_by; }
    public void setModifiedBy(String modified_by) { this.modified_by = modified_by; }

    @JsonProperty("modified_on")
    public Date getModifiedOn() { return this.modified_on; }
    public void setModifiedOn(Date modified_on) { this.modified_on = modified_on; }

    /**
     * Ensures that the _context of the asset is populated (takes no action if already populated)
     *
     * @param igcrest - a REST API connection to use in populating the context
     * @return Boolean indicating whether _context was successfully / already populated (true) or not (false)
     */
    public Boolean populateContext(IGCRestClient igcrest) {
        Boolean success = true;
        // Only bother retrieving the context if it isn't already present
        if (this.name == null && this._context.size() == 0) {
            IGCSearchCondition idOnly = new IGCSearchCondition("_id", "=", this.getId());
            IGCSearchConditionSet idOnlySet = new IGCSearchConditionSet(idOnly);
            IGCSearch igcSearch = new IGCSearch(this.getType(), idOnlySet);
            igcSearch.setPageSize(2);
            ReferenceList assetsWithCtx = igcrest.search(igcSearch);
            success = (assetsWithCtx.getItems().size() > 0);
            if (success) {
                Reference assetWithCtx = assetsWithCtx.getItems().get(0);
                this.name = assetWithCtx.getName();
                this._context = ((MainObject)assetWithCtx)._context;
            }
        }
        return success;
    }

    /**
     * Retrieves the semantic identity of the asset
     *
     * @param igcrest - a REST API connection to use in confirming the identity of the asset
     * @return Identity
     */
    public Identity getIdentity(IGCRestClient igcrest) {
        if (this.identity == null) {
            this.populateContext(igcrest);
            this.identity = new Identity(this._context, this.getType(), this.getName());
        }
        return this.identity;
    }

}
