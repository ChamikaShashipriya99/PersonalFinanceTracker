package com.example.finance.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finance.R
import com.example.finance.data.model.Transaction

/**
 * Adapter for displaying transactions in a RecyclerView.
 * @param transactions List of transactions to display.
 * @param onEdit Callback for editing a transaction.
 * @param onDelete Callback for deleting a transaction.
 */
class TransactionAdapter(
    private val transactions: MutableList<Transaction>,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvTitle.text = transaction.title
        holder.tvDate.text = transaction.date
        holder.tvAmount.text = if (transaction.type == "Income") "+LKR. ${transaction.amount}" else "-LKR. ${transaction.amount}"
        holder.tvAmount.setTextColor(
            if (transaction.type == "Income") Color.GREEN else Color.RED
        )
        holder.btnEdit.setOnClickListener { onEdit(transaction) }
        holder.btnDelete.setOnClickListener { onDelete(transaction) }
    }

    override fun getItemCount(): Int = transactions.size

    /**
     * Updates the transaction list and notifies the adapter.
     * @param newTransactions New list of transactions.
     */
    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }
}