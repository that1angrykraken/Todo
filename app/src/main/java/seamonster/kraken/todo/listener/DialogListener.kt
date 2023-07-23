package seamonster.kraken.todo.listener

interface DialogListener {
    fun onDismiss(selectedId: Int)
    fun onCancel()
}