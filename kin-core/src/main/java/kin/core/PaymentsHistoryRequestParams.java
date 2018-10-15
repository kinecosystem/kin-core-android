package kin.core;


/**
 * Class which build optional params for the payments history request.
 * @see KinAccount#getPaymentsHistory(PaymentsHistoryRequestParams requestParams)
 */
public class PaymentsHistoryRequestParams{

//    private final String cursor;
    private final int limit;
    private final Order order;

    /**
     * Represents possible order by values.
     */
    public enum Order {
        ASC("asc"),
        DESC("desc");

        private final String value;

        Order(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private PaymentsHistoryRequestParams(Builder builder) {
        this.limit = builder.limit;
        this.order = builder.order;
    }

    public int getLimit() {
        return limit;
    }

    public Order getOrder() {
        return order;
    }


    public static class Builder {

        private int limit;                  //optional
        private Order order;                //optional

//        /**
//         * @param token: is optional, a paging token(cursor), specifying where to start returning records from. for example 12884905984
//         * @return
//         */
//        public PaymentsHistoryRequestParamsBuilder cursor(String token) {
//            this.cursor = token;
//            return this;
//        }

        /**
         * @param limit is optional, a number, currently the default is 10. It is represents the maximum number of records to return.
         * @return
         */
        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        /**
         * @param order is optional, an Order, currently the default is "asc".	It is represents the order in which to return rows, “asc” or “desc”.
         * @return
         */
        public Builder order(Order order) {
            this.order = order;
            return this;
        }

        public PaymentsHistoryRequestParams build() {
            return new PaymentsHistoryRequestParams(this);
        }

    }
}
