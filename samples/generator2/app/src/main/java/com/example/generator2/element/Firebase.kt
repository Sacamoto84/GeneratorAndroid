package com.example.generator2.element

import android.content.Context
import timber.log.Timber


data class LoadingState constructor(var status: Status, var msg: String? = null, var msginfo: String = " ") {

    companion object {
        val LOADED  = LoadingState(Status.SUCCESS)
        val IDLE    = LoadingState(Status.IDLE)
        val LOADING = LoadingState(Status.RUNNING)
        fun error(msg: String?) = LoadingState(Status.FAILED, msg)
        fun info(msg: String) = LoadingState(Status.INFO, null , msg)
    }

    enum class Status {
        RUNNING, SUCCESS, FAILED, IDLE, INFO,
    }
}


class Firebas(val context: Context) {

    init {
        Timber.i("Firebas() init{}")
    }

//    var auth: FirebaseAuth? = null
//
//    //UI
//    var email by mutableStateOf("")
//    var password by mutableStateOf("")
//
//    var componentActivity: ComponentActivity? = null
//    var uid by mutableStateOf("")
//
//    //Firebase
//    val loadingState = MutableStateFlow(LoadingState.IDLE)
//
//    fun updateUI() {
//        uid = auth?.currentUser?.uid.toString()
//    }
//
//    fun reload() {
//        auth?.currentUser!!.reload().addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                updateUI()
//                //Toast.makeText(context, "Reload successful!", Toast.LENGTH_SHORT).show()
//            } else {
//                Log.e("Firebase", "reload", task.exception)
//                //Toast.makeText(context, "Failed to reload user.", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    @OptIn(DelicateCoroutinesApi::class)
//    fun createAccount(email: String, password: String, state: LoadingState) {
//        Log.d("Firebase", "createAccount:$email")
//        if (!validateForm()) {
//            return
//        }
//        GlobalScope.launch {
//            loadingState.emit(LoadingState.LOADING)
//        }
//        auth?.createUserWithEmailAndPassword(email, password)
//            ?.addOnCompleteListener(componentActivity!!) { task ->
//
//                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
//                    Log.d(
//                        "Firebase", "createUserWithEmail:success"
//                    ) //Toast.makeText( context, "createUserWithEmail:success", Toast.LENGTH_LONG ).show()
//                    val user = auth?.currentUser
//                    updateUI()
//
//                    GlobalScope.launch {
//                        loadingState.emit(LoadingState.LOADED)
//                    }
//
//                } else { // If sign in fails, display a message to the user.
//                    Log.w(
//                        "Firebase", "createUserWithEmail:failure", task.exception
//                    ) //Toast.makeText( context, "Authentication failed. ${task.exception}", Toast.LENGTH_LONG ).show() //updateUI(null)
//
//                    GlobalScope.launch {
//                        loadingState.emit(LoadingState.IDLE)
//                        state.status = LoadingState.Status.FAILED
//                        state.msg = task.exception.toString()
//                    }
//
//                }
//
//
//            }
//    }
//
//    private fun validateForm(): Boolean {
//        var valid = true
//        if (email.isEmpty()) valid = false
//        if (password.isEmpty()) valid = false
//        return valid
//    }
//
//    @OptIn(DelicateCoroutinesApi::class)
//    fun signInWithEmailAndPassword(email: String, password: String) = GlobalScope.launch {
//        try {
//            loadingState.emit(LoadingState.LOADING)
//            Firebase.auth.signInWithEmailAndPassword(email, password).await()
//            loadingState.emit(LoadingState.LOADED)
//            uid = auth?.currentUser?.uid.toString()
//            readMetaBackupFromFirebase(uid)
//        } catch (e: Exception) {
//            loadingState.emit(LoadingState.error(e.localizedMessage))
//        }
//    }
//
//    @OptIn(DelicateCoroutinesApi::class)
//    fun signWithCredential(credential: AuthCredential) = GlobalScope.launch {
//        try {
//            loadingState.emit(LoadingState.LOADING)
//            Firebase.auth.signInWithCredential(credential).await()
//            loadingState.emit(LoadingState.LOADED)
//            uid = auth?.currentUser?.uid.toString()
//            readMetaBackupFromFirebase(uid)
//        } catch (e: Exception) {
//            loadingState.emit(LoadingState.error(e.localizedMessage))
//        }
//    }

}