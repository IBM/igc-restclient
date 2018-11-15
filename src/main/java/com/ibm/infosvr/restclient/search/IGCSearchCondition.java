/* SPDX-License-Identifier: Apache-2.0 */
package com.ibm.infosvr.restclient.search;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class IGCSearchCondition {

    private JsonNodeFactory nf = JsonNodeFactory.instance;

    private String property;
    private String operator;
    private String value = null;

    private Boolean negated = null;

    public IGCSearchCondition(String property, String operator, String value) {
        this.property = property;
        this.operator = operator;
        this.value = value;
    }

    public IGCSearchCondition(String property, String operator, Boolean negated) {
        this.property = property;
        this.operator = operator;
        this.negated = negated;
    }

    public IGCSearchCondition(String property, String operator, String value, Boolean negated) {
        this.property = property;
        this.operator = operator;
        this.value = value;
        this.negated = negated;
    }

    public String getProperty() {
        return this.property;
    }

    public String getOperator() {
        return this.operator;
    }

    public String getValue() {
        return this.value;
    }

    public Boolean getNegated() {
        return this.negated;
    }

    /**
     * Returns the JSON object representing the condition
     *
     * @return ObjectNode
     */
    public ObjectNode getConditionObject() {
        ObjectNode condObj = nf.objectNode();
        condObj.set("property", nf.textNode(getProperty()));
        condObj.set("operator", nf.textNode(getOperator()));
        if (this.value != null) {
            condObj.set("value", nf.textNode(getValue()));
        }
        if (this.negated != null) {
            condObj.set("negated", nf.booleanNode(getNegated()));
        }
        return condObj;
    }

}
