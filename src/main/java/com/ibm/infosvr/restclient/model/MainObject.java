package com.ibm.infosvr.restclient.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ibm.infosvr.restclient.IGCRestClient;
import com.ibm.infosvr.restclient.IGCSearch;
import com.ibm.infosvr.restclient.IGCSearchCondition;
import com.ibm.infosvr.restclient.IGCSearchConditionSet;

import java.util.ArrayList;
import java.util.Date;

/**
 * The supertype of the vast majority of IGC objects.
 *
 * Simply define a new POJO as extending this base class to inherit the attributes that are found
 * on virtually all IGC asset types.
 */
public abstract class MainObject extends Reference {

    @JsonIgnore public Identity identity = null;

    public ArrayList<Reference> _context = new ArrayList<Reference>();

    public String name = null;
    public String short_description = null;
    public String long_description = null;

    public ReferenceList labels = null;
    public ReferenceList stewards = null;
    public ReferenceList assigned_to_terms = null;
    public ReferenceList implements_rules = null;
    public ReferenceList governed_by_rules = null;

    public String created_by;
    public Date created_on;
    public String modified_by;
    public Date modified_on;

    // TODO: add notes object reference

    public Boolean populateContext(IGCRestClient igcrest) {
        Boolean success = true;
        // Only bother retrieving the context if it isn't already present
        if (this.name == null && this._context.size() == 0) {
            IGCSearchCondition idOnly = new IGCSearchCondition("_id", "=", this._id);
            IGCSearchConditionSet idOnlySet = new IGCSearchConditionSet(idOnly);
            IGCSearch igcSearch = new IGCSearch(this._type, idOnlySet);
            igcSearch.setPageSize(2);
            ReferenceList assetsWithCtx = igcrest.search(igcSearch);
            success = (assetsWithCtx.items.size() > 0);
            if (success) {
                Reference assetWithCtx = assetsWithCtx.items.get(0);
                this.name = assetWithCtx._name;
                this._context = ((MainObject)assetWithCtx)._context;
            }
        }
        return success;
    }

    public Identity getIdentity(IGCRestClient igcrest) {
        if (this.identity == null) {
            this.populateContext(igcrest);
            this.identity = new Identity(this._context, this._type, this._name);
        }
        return this.identity;
    }

}
