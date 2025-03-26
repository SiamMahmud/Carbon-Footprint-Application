package com.example.carbonfootprintcalculation.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.dashboard.model.CarResult
import com.example.carbonfootprintcalculation.dashboard.model.User

class AdminViewUserAdapter(private var userList: ArrayList<User>, internal var context: Context) : RecyclerView.Adapter<AdminViewUserAdapter.PersonViewHolder>() {
    var onItemClick:((User) -> Unit)? = null
    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.userName)
        val userEmail: TextView = itemView.findViewById(R.id.userEmail)
        val card: CardView = itemView.findViewById(R.id.userProfile)


        fun bind(user: User) {
            userName.text = user.name
            userEmail.text = user.email
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_profile_item, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val item = userList[position]
        holder.bind(item)
        holder.card.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }
    override fun getItemCount(): Int = userList.size
}
