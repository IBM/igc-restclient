/* SPDX-License-Identifier: Apache-2.0 */
package com.ibm.infosvr.restclient.search;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class IGCSearchConditionSet {

    private JsonNodeFactory nf = JsonNodeFactory.instance;

    private ArrayNode conditions;
    private String conditionJoin = "and";

    private IGCSearchConditionSet nestedConditions;

    public IGCSearchConditionSet() {
        this.conditions = nf.arrayNode();
    }

    public IGCSearchConditionSet(IGCSearchCondition condition) {
        this();
        addCondition(condition);
    }

    /**
     * Adds the provided condition to the set of criteria to use in the search
     *
     * @param condition
     */
    public void addCondition(IGCSearchCondition condition) {
        this.conditions.add(condition.getConditionObject());
    }

    /**
     * Set whether to retrieve results for any condition match (true) or all conditions matching (false)
     *
     * @param on
     */
    public void setMatchAnyCondition(boolean on) {
        this.conditionJoin = on ? "or" : "and";
    }

    /**
     * Adds a set of conditions as nested conditions of this set
     *
     * @param igcConditions
     */
    public void addNestedConditionSet(IGCSearchConditionSet igcConditions) {
        this.nestedConditions = igcConditions;
    }

    /**
     * Returns the number of conditions in the set (0 if none)
     *
     * @return int
     */
    public int size() {
        return this.conditions.size();
    }

    /**
     * Returns the JSON structure for the set of conditions
     *
     * @return ObjectNode
     */
    public ObjectNode getConditionSetObject() {
        ObjectNode condSet = nf.objectNode();
        if (size() > 0) {
            condSet.set("conditions", this.conditions);
            condSet.set("operator", nf.textNode(this.conditionJoin));
        }
        if (nestedConditions != null) {
            ArrayNode condSetOuter = (ArrayNode) condSet.get("conditions");
            condSetOuter.add(nestedConditions.getConditionSetObject());
        }
        return condSet;
    }

}
