package seamonster.kraken.todo.util

import seamonster.kraken.todo.model.Task

class TaskComparator : Comparator<Task>{
    override fun compare(t1: Task, t2: Task): Int {
        var c = t1.year.compareTo(t2.year)
        if(c != 0) return c
        c = t1.month.compareTo(t2.month)
        if(c != 0) return c
        c = t1.date.compareTo(t2.date)
        if(c != 0) return c
        c = t1.hour!!.compareTo(t2.hour!!)
        if(c != 0) return c
        return t1.minute!!.compareTo(t2.minute!!)
    }
}