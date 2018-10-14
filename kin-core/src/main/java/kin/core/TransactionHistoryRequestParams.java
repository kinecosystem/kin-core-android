package kin.core;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class which build optional params for the transaction payment history request.
 */
public class TransactionHistoryRequestParams implements Parcelable {

    private String accountId;
    private final String token;
    private final int limit;
    private final Order order;

    /**
     * Represents possible <code>order</code> parameter values.
     */
    public enum Order { // TODO: 12/10/2018 add it only because I am not sure we can use the stellar sdk Order Enum
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

    private TransactionHistoryRequestParams(TransactionHistoryRequestParamsBuilder builder) {
        this.accountId = builder.accountId;
        this.token = builder.token;
        this.limit = builder.limit;
        this.order = builder.order;
    }

    protected TransactionHistoryRequestParams(Parcel in) {
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

    public static final Creator<TransactionHistoryRequestParams> CREATOR = new Creator<TransactionHistoryRequestParams>() {
        @Override
        public TransactionHistoryRequestParams createFromParcel(Parcel in) {
            return new TransactionHistoryRequestParams(in);
        }

        @Override
        public TransactionHistoryRequestParams[] newArray(int size) {
            return new TransactionHistoryRequestParams[size];
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


    public static class TransactionHistoryRequestParamsBuilder {

        private String accountId;           //optional
        private String token;               //optional
        private int limit;                  //optional
        private Order order;                //optional

        public TransactionHistoryRequestParamsBuilder account(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public TransactionHistoryRequestParamsBuilder cursor(String token) {
            this.token = token;
            return this;
        }

        public TransactionHistoryRequestParamsBuilder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public TransactionHistoryRequestParamsBuilder order(Order order) {
            this.order = order;
            return this;
        }

        public TransactionHistoryRequestParams build() {
            return new TransactionHistoryRequestParams(this);
        }

    }

}
