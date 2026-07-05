package com.poc.orderservice.util;

public final class OrderConstants {

    private OrderConstants() {
    }

    public static final class Status {

        private Status() {
        }

        public static final String DRAFT = "DRAFT";

        public static final String DELETED = "DELETED";

        public static final String CREATED = "CREATED";

        public static final String SUBMITTED = "SUBMITTED";
    }

    public static final class Remarks {

        private Remarks() {
        }

        public static final String ORDER_DRAFTED = "Order drafted";

        public static final String ORDER_UPDATED = "Order updated";

        public static final String ORDER_DELETED = "Order deleted";

        public static final String ORDER_CREATED = "Order validated and created for submission";

        public static final String ORDER_SUBMITTED = "Order submitted";
    }
}
