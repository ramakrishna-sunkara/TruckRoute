package com.truckroute.ecoway.adapaters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.truckroute.ecoway.R
import com.truckroute.ecoway.models.LeaderBoardModel

class LeaderBoardListAdapter(private val instructions: List<LeaderBoardModel>) :
    RecyclerView.Adapter<LeaderBoardListAdapter.ViewHolder>() {

    lateinit var context: Context
    private var leaderBoardIconProvider: LeaderBoardIconProvider = LeaderBoardIconProvider()

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_item_leader_board, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val instruction = instructions[position]
        holder.txtName.text = instruction.name
        holder.imgProfileIcon.setImageDrawable(leaderBoardIconProvider.getProfileIcon(context, instruction))
        holder.txtEarnings.text = "(${instruction.earned})"
        when(instruction.position){
            1 -> {
                holder.imgPosition.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.position1))
                holder.imgPosition.visibility = View.VISIBLE
                holder.txtPosition.visibility = View.GONE
            }
            2 -> {
                holder.imgPosition.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.position2))
                holder.imgPosition.visibility = View.VISIBLE
                holder.txtPosition.visibility = View.GONE
            }
            3 -> {
                holder.imgPosition.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.position3))
                holder.imgPosition.visibility = View.VISIBLE
                holder.txtPosition.visibility = View.GONE
            }
            else -> {
                holder.imgPosition.visibility = View.GONE
                holder.txtPosition.visibility = View.VISIBLE
                holder.txtPosition.text = instruction.position.toString()
            }
        }
        holder.txtPoints.text = "${instruction.points.toString()} points"
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return instructions.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtEarnings: TextView = itemView.findViewById(R.id.txtEarnings)
        val txtPoints: TextView = itemView.findViewById(R.id.txtPoints)
        val txtPosition: TextView = itemView.findViewById(R.id.txtPosition)
        val imgProfileIcon: ImageView = itemView.findViewById(R.id.imgProfileIcon)
        val imgPosition: ImageView = itemView.findViewById(R.id.imgPosition)
    }
}
