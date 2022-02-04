package com.truckroute.ecoway.adapaters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tomtom.online.sdk.routing.route.information.Instruction
import com.truckroute.ecoway.R

class ManeuversListAdapter(private val instructions: List<Instruction>) :
    RecyclerView.Adapter<ManeuversListAdapter.ViewHolder>() {

    companion object {
        private const val METERS_IN_KM = 1000
    }

    lateinit var context: Context
    private var iconManeuverProvider: ManeuversIconProvider = ManeuversIconProvider()

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_item_maneuver_instruction, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val instruction = instructions[position]
        holder.description.text = instruction.message
        holder.icon.setImageDrawable(iconManeuverProvider.getIcon(context, instruction))
        val metersToManeuver = instruction.routeOffsetInMeters
        val kilometersToManeuver = metersToManeuver / METERS_IN_KM
        if (metersToManeuver > METERS_IN_KM) {
            holder.distance.text = context.getString(R.string.distance_km, kilometersToManeuver)
        } else {
            holder.distance.text = context.getString(R.string.distance_m, metersToManeuver)
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return instructions.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val description: TextView = itemView.findViewById(R.id.maneuver_description)
        val icon: ImageView = itemView.findViewById(R.id.maneuver_icon)
        val distance: TextView = itemView.findViewById(R.id.maneuver_distance)
    }
}
