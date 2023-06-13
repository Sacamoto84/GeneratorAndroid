package libs

/*
class MyViewModel : ViewModel() {
    /**
     * Heavy operation that cannot be done in the Main Thread
     */
    fun launchDataLoad() {
*       viewModelScope.launch {
            sortList()
            // Modify UI
        }
    }
    suspend fun sortList() = withContext(Dispatchers.Default) {
        // Heavy work
    }
}
*/