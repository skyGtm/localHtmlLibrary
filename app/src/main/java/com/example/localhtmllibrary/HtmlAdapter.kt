package com.example.localhtmllibrary

import android.content.Context
import android.graphics.Color
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class HtmlAdapter(
    private val context: Context,
    val items: MutableList<HtmlFile>,
    private val click: (HtmlFile) -> Unit,
    private val pin: (HtmlFile) -> Unit
) : RecyclerView.Adapter<HtmlAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(context).inflate(R.layout.item_html, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val x = items[pos]
        val card = h.itemView.findViewById<View>(R.id.card)
        val title = h.itemView.findViewById<TextView>(R.id.title)
        val pin = h.itemView.findViewById<TextView>(R.id.pin)
        title.text = x.title
        pin.text = if (x.pinned) "★" else "☆"
        card.setOnClickListener { click(x) }
        card.setOnLongClickListener { pin(x); true }
        pin.setOnClickListener { pin(x) }
    }

    override fun getItemCount() = items.size
}
