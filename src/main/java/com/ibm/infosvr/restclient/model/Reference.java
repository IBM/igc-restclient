package com.ibm.infosvr.restclient.model;

import com.fasterxml.jackson.annotation.*;
import com.ibm.infosvr.restclient.IGCRestClient;
import com.ibm.infosvr.restclient.IGCSearch;
import com.ibm.infosvr.restclient.IGCSearchCondition;
import com.ibm.infosvr.restclient.IGCSearchConditionSet;

import java.lang.reflect.Field;

/**
 * The ultimate parent object for IGC assets, it contains only the most basic information common to every single
 * asset in IGC, and present in every single reference to an IGC asset (whether via relationship, search result,
 * etc):
 *  - _name
 *  - _type
 *  - _id
 *  - _url
 *
 *  Generally POJOs should not extend this class directly, but the MainObject class.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="_type", visible=true, defaultImpl=Reference.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Reference extends ObjectPrinter {

    public String _name;
    public String _type;
    public String _id;
    public String _url;

    /**
     * This will generally be the most performant method by which to retrieve asset information, when only
     * some subset of properties is required
     *
     * @param igcrest - the IGCRestClient connection to use to retrieve the details
     * @param properties - a list of the properties to retrieve
     * @return Reference - the object including only the subset of properties specified
     */
    public Reference getAssetWithSubsetOfProperties(IGCRestClient igcrest, String[] properties) {
        Reference assetWithProperties = null;
        IGCSearchCondition idOnly = new IGCSearchCondition("_id", "=", this._id);
        IGCSearchConditionSet idOnlySet = new IGCSearchConditionSet(idOnly);
        IGCSearch igcSearch = new IGCSearch(this._type, properties, idOnlySet);
        igcSearch.setPageSize(2);
        ReferenceList assetsWithProperties = igcrest.search(igcSearch);
        if (assetsWithProperties.items.size() > 0) {
            assetWithProperties = assetsWithProperties.items.get(0);
        }
        return assetWithProperties;
    }

    /**
     * Retrieve the asset details from a minimal reference stub
     *
     * Be sure to first use the IGCRestClient "registerPOJO" method to register your POJO(s) for interpretting the
     * type(s) for which you're interested in retrieving details
     *
     * Note that this will only include the first page of any relationships -- to also retrieve all
     * relationships see getFullAssetDetails
     *
     * @param igcrest - the IGCRestClient connection to use to retrieve the details
     * @return Reference - the object including all of its details
     */
    public Reference getAssetDetails(IGCRestClient igcrest) {
        return igcrest.getAssetById(this._id);
    }

    /**
     * Retrieve all of the asset details, including all relationships, from a minimal reference stub
     *
     * Be sure to first use the IGCRestClient "registerPOJO" method to register your POJO(s) for interpretting the
     * type(s) for which you're interested in retrieving details
     *
     * Note that this is quite a heavy operation, relying on multiple REST calls, to build up what could be a very
     * large object; to simply retrieve the details without all relationships, see getAssetDetails
     *
     * @param igcrest - the IGCRestClient connection to use to retrieve the details and relationships
     * @return Reference - the object including all of its details and relationships
     */
    public Reference getFullAssetDetails(IGCRestClient igcrest) {
        Reference asset = this.getAssetDetails(igcrest);
        try {
            for (Field f : getClass().getFields()) {
                Class fieldClass = f.getType();
                if (fieldClass == ReferenceList.class) {
                    // Uses reflection to call getAllPages on any ReferenceList fields
                    Object relationship = f.get(asset);
                    if (relationship != null) {
                        ((ReferenceList) relationship).getAllPages(igcrest);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return asset;
    }

    // TODO: eventually handle the '_expand' that exists for data classifications, eg.:
    /*
        "_expand": {
          "data_class": {
            "_name": "NoClassDetected",
            "_type": "data_class",
            "_id": "f4951817.e469fa50.000mds3r3.50jf89j.ft6i2i.tj0uog4pijcjhn7orjq9c",
            "_url": "https://infosvr.vagrant.ibm.com:9446/ibm/iis/igc-rest/v1/assets/f4951817.e469fa50.000mds3r3.50jf89j.ft6i2i.tj0uog4pijcjhn7orjq9c"
          },
          "confidencePercent": "12",
          "_repr_obj": {
            "_name": "SALARY",
            "_type": "classification",
            "_id": "f4951817.db110006.000mdsmfl.t03b2vr.da3ov9.555mlr126kisjn0coh9e5",
            "_url": "https://infosvr.vagrant.ibm.com:9446/ibm/iis/igc-rest/v1/assets/f4951817.db110006.000mdsmfl.t03b2vr.da3ov9.555mlr126kisjn0coh9e5"
          },
          "detectedState": "63.16"
        },
     */

}
