package br.com.optimizer7.logworktime

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.optimizer7.logworktime.Model.Worktime
import kotlinx.android.synthetic.main.logged_worktime_cell.view.*

class ListLoggedWorktimeAdapter(private val loggedWorktimes: ArrayList<Worktime>) : RecyclerView.Adapter<ListLoggedWorktimeAdapter.PlaceHolder>(){
    val mDataSet: MutableList<Worktime> = mutableListOf()

    override fun onBindViewHolder(holder: ListLoggedWorktimeAdapter.PlaceHolder, position: Int) {
        val itemWorktime = loggedWorktimes[position]
        holder.bindWorktime(itemWorktime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListLoggedWorktimeAdapter.PlaceHolder{
//        if(viewType == 1){
//            val inflater = LayoutInflater.from(parent.context)
//            val inflatedView = inflater.inflate(R.layout.logged_worktime_label_cell, parent, false)
//            return PlaceHolder(inflatedView, viewType)
//        }else{
            val inflater = LayoutInflater.from(parent.context)
            val inflatedView = inflater.inflate(R.layout.logged_worktime_cell, parent, false)
            return PlaceHolder(inflatedView)
//        }
    }

    override fun getItemCount(): Int {
        return loggedWorktimes.size
    }

//    override fun getItemViewType(position: Int): Int {
//        if(position == 0) return 1
//        else return 2
//    }



    class PlaceHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener{

        var view: View = v
        var worktime: Worktime? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val context = itemView.context
        }

        fun bindWorktime(worktime: Worktime){
//                view.startWorktime.text = view.context.getString(R.string.start_worktime_label)
//                view.startLunch.text = view.context.getString(R.string.lunch_worktime_label)
//                view.doneLunch.text = view.context.getString(R.string.lunch_end_worktime_label)
//                view.doneWorktime.text = view.context.getString(R.string.finish_worktime_label)
//                view.dateWorktime.text = view.context.getString(R.string.worktime_date)
//            }else{
                this.worktime = worktime
                view.startWorktime.setText(IsEmptyOrNull(worktime.beginWorktime))
                view.startLunch.setText(IsEmptyOrNull(worktime.beginLunch))
                view.doneLunch.setText(IsEmptyOrNull(worktime.doneLunch))
                view.doneWorktime.setText(IsEmptyOrNull(worktime.doneWorktime))
                view.dateWorktime.setText(worktime.date)
//            }
        }

        fun IsEmptyOrNull(data: String?) : String{
            if(data.equals("") || data == null){
                return "N/A"
            }
            else{
                return data
            }
        }
    }
}