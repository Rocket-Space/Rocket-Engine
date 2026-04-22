package it.pixiekevin.rocketengine.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GoogleAuthManager(context: Context) {
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN.getScopes()
            )
            .build()
        
        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun handleSignInResult(data: Intent?): GoogleSignInAccount? = suspendCancellableCoroutine { continuation ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        
        task.addOnSuccessListener { account ->
            continuation.resume(account)
        }
        
        task.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(googleSignInClient.applicationContext)
    }

    fun signOut() {
        googleSignInClient.signOut()
    }

    fun isSignedIn(): Boolean {
        return getLastSignedInAccount() != null
    }

    fun getAccountEmail(): String? {
        return getLastSignedInAccount()?.email
    }

    fun getAccountDisplayName(): String? {
        return getLastSignedInAccount()?.displayName
    }

    fun getAccountPhotoUrl(): String? {
        return getLastSignedInAccount()?.photoUrl?.toString()
    }
}
