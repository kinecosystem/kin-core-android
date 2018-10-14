package kin.core.sample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import kin.core.PaymentInfo;
import kin.sdk.core.sample.R;

class PaymentsHistoryRecyclerViewAdapter extends RecyclerView.Adapter<PaymentsHistoryRecyclerViewAdapter.TransactionHistoryViewHolder> {

    private List<PaymentInfo> payments;

    PaymentsHistoryRecyclerViewAdapter(List<PaymentInfo> payments) {
        this.payments = payments;
    }

    @Override
    public int getItemCount() {
        if(payments != null){
            return payments.size();
        }
        return 0;
    }

    @NonNull
    @Override
    public TransactionHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_info,parent,false);
        return new TransactionHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionHistoryViewHolder holder, int position) {
        PaymentInfo paymentInfo = payments.get(position);
        holder.destinationText.setText(paymentInfo.destinationPublicKey());
        holder.sourceText.setText(paymentInfo.sourcePublicKey());
        holder.hashText.setText(paymentInfo.hash().id());
        holder.amountText.setText(paymentInfo.amount().toPlainString());
        holder.createdAtText.setText(paymentInfo.createdAt());
        if (TextUtils.isEmpty(holder.memoText.getText())) {
            holder.memoText.setVisibility(View.GONE);
        } else {
            holder.memoText.setText(paymentInfo.memo());
            holder.memoText.setVisibility(View.VISIBLE);
        }
    }

//    public void addAll(List<PaymentInfo> payments) {
//        if (payments != null) {
//            // Could also use DiffUtil in order to replace only the changed items in case we will do a refresh or something like that.
//            this.payments.clear();
//            this.payments.addAll(payments);
//            notifyDataSetChanged();
//        }
//    }

    class TransactionHistoryViewHolder extends RecyclerView.ViewHolder {

        TextView destinationText;
        TextView sourceText;
        TextView amountText;
        TextView memoText;
        TextView hashText;
        TextView createdAtText;

        TransactionHistoryViewHolder(View itemView) {
            super(itemView);
            destinationText = itemView.findViewById(R.id.to_public_id);
            sourceText = itemView.findViewById(R.id.from_public_id);
            amountText = itemView.findViewById(R.id.amount);
            memoText = itemView.findViewById(R.id.memo);
            hashText = itemView.findViewById(R.id.tx_hash);
            createdAtText = itemView.findViewById(R.id.created_at);
        }
    }


}

