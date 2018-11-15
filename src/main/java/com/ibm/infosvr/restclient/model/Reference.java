/* SPDX-License-Identifier: Apache-2.0 */
package com.ibm.infosvr.restclient.model;

import com.fasterxml.jackson.annotation.*;
import com.ibm.infosvr.restclient.IGCRestClient;
import com.ibm.infosvr.restclient.search.IGCSearch;
import com.ibm.infosvr.restclient.search.IGCSearchCondition;
import com.ibm.infosvr.restclient.search.IGCSearchConditionSet;

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

    protected String _name;
    protected String _type;
    protected String _id;
    protected String _url;

    @JsonProperty("_name")
    public String getName() { return this._name; }
    public void setName(String _name) { this._name = _name; }

    @JsonProperty("_type")
    public String getType() { return this._type; }
    public void setType(String _type) { this._type = _type; }

    @JsonProperty("_id")
    public String getId() { return this._id; }
    public void setId(String _id) { this._id = _id; }

    @JsonProperty("_url")
    public String getUrl() { return this._url; }
    public void setUrl(String _url) { this._url = _url; }

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
        if (assetsWithProperties.getItems().size() > 0) {
            assetWithProperties = assetsWithProperties.getItems().get(0);
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

    /**
     * Recursively traverses the class hierarchy upwards to find the field
     *
     * @param name - the name of the field to find
     * @param clazz - the class in which to search (and recurse upwards on its class hierarchy)
     * @return Field first found (lowest level of class hierarchy), or null if never found
     */
    private static Field _recursePropertyByName(String name, Class clazz) {
        Field f = null;
        Class superClazz = clazz.getSuperclass();
        if (superClazz != null) {
            try {
                f = superClazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                f = Reference._recursePropertyByName(name, superClazz);
            }
        }
        return f;
    }

    /**
     * Retrieves the first Field, from anywhere within the class hierarchy (bottom-up), by its name
     *
     * @param name - the name of the field to retrieve
     * @return Field
     */
    public Field getFieldByName(String name) {
        Field field;
        try {
            field = this.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            field = Reference._recursePropertyByName(name, this.getClass());
        }
        return field;
    }

    /**
     * Retrieves the value of a property of this asset by the provided name (allows dynamic retrieval of properties)
     *
     * @param name - the property name to retrieve
     * @return Object - an object representing that property (eg. String, Reference, etc)
     */
    public Object getPropertyByName(String name) {
        Object value = null;
        Field property = getFieldByName(name);
        if (property != null) {
            try {
                value = property.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    /**
     * Returns true iff the provided object is a relationship (ie. of class Reference)
     *
     * @param obj
     * @return Boolean
     */
    public static final Boolean isReference(Object obj) {
        return (obj.getClass() == Reference.class);
    }

    /**
     * Returns true iff the provided property name of this object is a relationship (ie. of class Reference)
     *
     * @param propertyName
     * @return Boolean
     */
    public Boolean isReference(String propertyName) {
        Field property = getFieldByName(propertyName);
        return (property.getType() == Reference.class);
    }

    /**
     * Returns true iff the provided object is a list of relationships (ie. of class ReferenceList)
     *
     * @param obj
     * @return Boolean
     */
    public static final Boolean isReferenceList(Object obj) {
        return (obj.getClass() == ReferenceList.class);
    }

    /**
     * Returns true iff the provided property name of this object is a list of relationships (ie. of class ReferenceList)
     *
     * @param propertyName
     * @return Boolean
     */
    public Boolean isReferenceList(String propertyName) {
        Field property = getFieldByName(propertyName);
        return (property.getType() == ReferenceList.class);
    }

    /**
     * Returns true iff the provided object is a simple type (String, Number, Boolean, Date, etc)
     *
     * @param obj
     * @return Boolean
     */
    public static final Boolean isSimpleType(Object obj) {
        return (!Reference.isReference(obj) && !Reference.isReferenceList(obj));
    }

    /**
     * Returns true iff the provided property name of this object is a simple type (String, Number, Boolean, Date, etc)
     *
     * @param propertyName
     * @return Boolean
     */
    public Boolean isSimpleType(String propertyName) {
        Field property = getFieldByName(propertyName);
        return (property.getType() != Reference.class && property.getType() != ReferenceList.class);
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
