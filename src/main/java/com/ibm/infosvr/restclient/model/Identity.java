package com.ibm.infosvr.restclient.model;

import java.util.ArrayList;

public class Identity {

    private ArrayList<String> typesInOrder;
    private ArrayList<String> namesInOrder;

    private String assetType;
    private String assetName;

    public Identity() {
        typesInOrder = new ArrayList<>();
        namesInOrder = new ArrayList<>();
        assetType = "";
        assetName = "";
    }

    public Identity(ArrayList<Reference> context, String assetType, String assetName) {
        this();
        for (Reference ref : context) {
            this.typesInOrder.add(ref._type);
            this.namesInOrder.add(ref._name);
        }
        this.assetType = assetType;
        this.assetName = assetName;
    }

    public Boolean equals(Identity identity) {
        return this.toString().equals(identity.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < typesInOrder.size(); i++) {
            String type = typesInOrder.get(i);
            String name = namesInOrder.get(i);
            sb.append("(" + type + ")=" + name + "::");
        }
        sb.append("(" + assetType + ")=" + assetName);
        return sb.toString();
    }

}
