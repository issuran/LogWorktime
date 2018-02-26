package br.com.optimizer7.logworktime

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.optimizer7.logworktime.Model.Worktime
import kotlinx.android.synthetic.main.logged_worktime_cell.view.*

class ListLoggedWorktimeAdapter(private val loggedWorktimes: ArrayList<Worktime>) : RecyclerView.Adapter<ListLoggedWorktimeAdapter.PlaceHolder>(){

    override fun onBindViewHolder(holder: ListLoggedWorktimeAdapter.PlaceHolder, position: Int) {
        if(position == 0){
            val itemWorktime = Worktime()
            holder.bindWorktime(itemWorktime, position - 1)
        }else{
            if(itemCount >= 1){
                val itemWorktime = loggedWorktimes[position - 1]
                holder.bindWorktime(itemWorktime, position - 1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListLoggedWorktimeAdapter.PlaceHolder{

        val inflater = LayoutInflater.from(parent.context)
        val inflatedView = inflater.inflate(R.layout.logged_worktime_cell, parent, false)
        return PlaceHolder(inflatedView)

    }

    override fun getItemCount(): Int {
        return loggedWorktimes.size + 1
    }

    class PlaceHolder(v: View) : RecyclerView.ViewHolder(v){

        private var view: View = v
        private var worktime: Worktime? = null

        fun bindWorktime(worktime: Worktime, position: Int){
            if( position == -1){
                view.startWorktime.text = view.context.getString(R.string.start_worktime_label)
                view.startLunch.text = view.context.getString(R.string.lunch_worktime_label)
                view.doneLunch.text = view.context.getString(R.string.lunch_end_worktime_label)
                view.doneWorktime.text = view.context.getString(R.string.finish_worktime_label)
                view.dateWorktime.text = view.context.getString(R.string.date)


                view.startWorktime.setTextColor(ContextCompat.getColor(view.context, R.color.colorBlackText))
                view.startLunch.setTextColor(ContextCompat.getColor(view.context, R.color.colorBlackText))
                view.doneLunch.setTextColor(ContextCompat.getColor(view.context, R.color.colorBlackText))
                view.doneWorktime.setTextColor(ContextCompat.getColor(view.context, R.color.colorBlackText))
                view.dateWorktime.setTextColor(ContextCompat.getColor(view.context, R.color.colorBlackText))
            }else{
                this.worktime = worktime
                view.startWorktime.text = isEmptyOrNull(worktime.beginWorktime)
                view.startLunch.text = isEmptyOrNull(worktime.beginLunch)
                view.doneLunch.text = isEmptyOrNull(worktime.doneLunch)
                view.doneWorktime.text = isEmptyOrNull(worktime.doneWorktime)
                view.dateWorktime.text = worktime.date

                view.dateWorktime.setTextColor(ContextCompat.getColor(view.context, R.color.colorPrimary))
            }
        }

        private fun isEmptyOrNull(data: String?) : String{
            return if(data.equals("") || data == null){
                "N/A"
            } else{
                data
            }
        }
    }
}