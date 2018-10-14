package kin.core;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class which build optional params for the transaction payment history request.
 */
public class PaymentsHistoryRequestParams implements Parcelable {

    private String accountId;
    private final String token;
    private final int limit;
    private final Order order;

    /**
     * Represents possible <code>order</code> parameter values.
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

    private PaymentsHistoryRequestParams(PaymentsHistoryRequestParamsBuilder builder) {
        this.accountId = builder.accountId;
        this.token = builder.token;
        this.limit = builder.limit;
        this.order = builder.order;
    }

    protected PaymentsHistoryRequestParams(Parcel in) {
        accountId = in.readString();
        token = in.readString();
        limit = in.readInt();
        String orderName = in.readString();
        order = orderName != null ? Order.valueOf(orderName) : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accountId);
        dest.writeString(token);
        dest.writeInt(limit);
        dest.writeString(order != null ? order.name() : null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PaymentsHistoryRequestParams> CREATOR = new Creator<PaymentsHistoryRequestParams>() {
        @Override
        public PaymentsHistoryRequestParams createFromParcel(Parcel in) {
            return new PaymentsHistoryRequestParams(in);
        }

        @Override
        public PaymentsHistoryRequestParams[] newArray(int size) {
            return new PaymentsHistoryRequestParams[size];
        }
    };

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getToken() {
        return token;
    }

    public int getLimit() {
        return limit;
    }

    public Order getOrder() {
        return order;
    }


    public static class PaymentsHistoryRequestParamsBuilder {

        private String accountId;           //optional
        private String token;               //optional
        private int limit;                  //optional
        private Order order;                //optional

        public PaymentsHistoryRequestParamsBuilder account(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public PaymentsHistoryRequestParamsBuilder cursor(String token) {
            this.token = token;
            return this;
        }

        public PaymentsHistoryRequestParamsBuilder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public PaymentsHistoryRequestParamsBuilder order(Order order) {
            this.order = order;
            return this;
        }

        public PaymentsHistoryRequestParams build() {
            return new PaymentsHistoryRequestParams(this);
        }

    }

}
