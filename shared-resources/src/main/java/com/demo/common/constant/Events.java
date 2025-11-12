package com.demo.common.constant;

public class Events {

    private Events() {}

    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_COMPLETED = "ORDER_COMPLETED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";

    public static final String PAYMENT_SUCCEEDED = "PAYMENT_SUCCEEDED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";

    public static final String PRODUCTS_UPDATED = "PRODUCTS_UPDATED";
    public static final String PRODUCTS_SHORTAGE = "PRODUCTS_SHORTAGE";
    public static final String PRODUCTS_RESTORED = "PRODUCTS_RESTORED";
    public static final String AVAILABILITY_CONFIRMED = "AVAILABILITY_CONFIRMED";

    public static final String SHIPMENT_ARRANGED = "SHIPMENT_ARRANGED";
    public static final String ARRANGEMENT_FAILED = "ARRANGEMENT_FAILED";
    public static final String SHIPMENT_CANCELLED = "SHIPMENT_CANCELLED";

    public static final String UNEXPECTED_ERROR = "UNEXPECTED_ERROR";

}
