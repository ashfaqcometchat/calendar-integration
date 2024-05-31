package com.cometchat.calendarintegration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.conversationswithmessages.CometChatConversationsWithMessages
import com.cometchat.chatuikit.messagecomposer.MessageComposerConfiguration
import com.cometchat.chatuikit.messageheader.MessageHeaderConfiguration
import com.cometchat.chatuikit.messageheader.MessageHeaderStyle
import com.cometchat.chatuikit.messages.MessagesConfiguration
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.cometchatuikit.UIKitSettings
import com.cometchat.chatuikit.shared.framework.ChatConfigurator
import com.cometchat.chatuikit.shared.models.CometChatMessageComposerAction
import com.cometchat.chatuikit.shared.views.CometChatBadge.BadgeStyle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import com.google.gson.Gson

private const val TAG = "MainActivity"
const val REQUEST_AUTHORIZATION = 1001

class MainActivity : AppCompatActivity() {
    private val SCOPES =
        listOf(CalendarScopes.CALENDAR, "https://www.googleapis.com/auth/calendar.events")
    private val RC_SIGN_IN = 1000
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var credential: GoogleAccountCredential

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initCometChat()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        val messageComposerConfiguration = MessageComposerConfiguration()

        messageComposerConfiguration.setAttachmentOption { context: Context?, user: User?, group: Group?, stringStringHashMap: HashMap<String?, String?>? ->
            val list =
                ChatConfigurator.getDataSource()
                    .getAttachmentOptions(context, user, group, stringStringHashMap)
            list.add(
                CometChatMessageComposerAction().setTitle("Schedule meeting").setId("scheduler")
                    .setOnClick {
                        signIn()
                    }
            )
            list
        }

        val conversationWithMessages: CometChatConversationsWithMessages =
            findViewById(R.id.conversationWithMessages)

        val messageHeaderStyle = MessageHeaderStyle()
        messageHeaderStyle.subtitleTextColor = ContextCompat.getColor(this, R.color.black)
        val messagesConfiguration = MessagesConfiguration()
        val messageHeaderConfiguration = MessageHeaderConfiguration()
        messageHeaderConfiguration.setStyle(messageHeaderStyle)
        messagesConfiguration.messageHeaderConfiguration = messageHeaderConfiguration
        conversationWithMessages.messagesConfiguration = messagesConfiguration

        val badgeStyle = BadgeStyle()
        badgeStyle.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_circle))
        conversationWithMessages.setBadgeStyle(badgeStyle)

        messagesConfiguration.messageComposerConfiguration = messageComposerConfiguration
        conversationWithMessages.messagesConfiguration = messagesConfiguration
    }

    private fun initCometChat() {
        val appID = "2560595837ef79ae"
        val region = "in"
        val authKey = "3c9d2e4725ad3a7821f4e14c711d9eae88f91357"

        val uiKitSettings = UIKitSettings.UIKitSettingsBuilder()
            .setRegion(region)
            .setAppId(appID)
            .setAuthKey(authKey)
            .subscribePresenceForAllUsers().build()

        CometChatUIKit.init(this, uiKitSettings, object : CometChat.CallbackListener<String?>() {
            override fun onSuccess(successString: String?) {
                CometChatUIKit.login("superhero1", object : CometChat.CallbackListener<User>() {
                    override fun onSuccess(user: User) {
                        Log.d(TAG, "Login Successful : $user")
                    }

                    override fun onError(e: CometChatException) {
                        Log.e(TAG, "Login Failed : " + e.message)
                    }
                })
            }

            override fun onError(e: CometChatException?) {}
        })
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else if (requestCode == REQUEST_AUTHORIZATION) {
            if (resultCode == RESULT_OK) {
                navigateToEventDetailsActivity()
            }
        }
    }

    private fun navigateToEventDetailsActivity() {
        val intent = Intent(this@MainActivity, EventDetailsActivity::class.java)
        val gson = Gson()
        val credentialJson = gson.toJson(credential)
        intent.putExtra("credential", credentialJson)
        startActivity(intent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let {
                credential = GoogleAccountCredential.usingOAuth2(
                    applicationContext, SCOPES
                ).setBackOff(ExponentialBackOff())
                credential.selectedAccount = account.account
                // Proceed with calendar API calls
                navigateToEventDetailsActivity()
            }
        } catch (e: ApiException) {
            e.printStackTrace()
        }
    }

//    private fun authorizeAndSendEvents() {
//        GlobalScope.launch {
//            try {
//                val tokenFolder = getTokenFolder()
//                if (!tokenFolder.exists()) {
//                    Log.d(TAG, "authorizeAndSendEvents: Test 1")
//                    tokenFolder.mkdirs()
//                    val credential = getCredentials(HTTP_TRANSPORT)
//                    initialize(credential)
//                    createEvent()
//                } else {
//                    val credential = getCredentials(HTTP_TRANSPORT)
//                    Log.d(TAG, "authorizeAndSendEvents: Test 5 credentials: $credential")
//
//                    initialize(credential)
//                    createEvent()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private fun getTokenFolder() =
//        File(this.getExternalFilesDir("")?.absolutePath + TOKENS_DIRECTORY_PATH)
//
//    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
//        Log.d(TAG, "getCredentials: Test 2")
//        val fileInputStream = this.assets.open(CREDENTIALS_FILE_PATH)
//        val clientSecret =
//            GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(fileInputStream))
//        val authorizationFlow =
//            GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecret, SCOPES)
//                .setDataStoreFactory(FileDataStoreFactory(getTokenFolder()))
//                .setAccessType("offline")
//                .build()
//
//        val authorizationCodeInstalledApp =
//            object : AuthorizationCodeInstalledApp(authorizationFlow, LocalServerReceiver()) {
//                override fun onAuthorization(authorizationUrl: AuthorizationCodeRequestUrl) {
//                    val url = authorizationUrl.build()
//                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                    Log.d(TAG, "onAuthorization: Test 4")
//                    Log.d(TAG, "onAuthorization: url: $url")
//                    startActivity(browserIntent)
//                }
//            }
//        Log.d(
//            TAG,
//            "Test 6 getCredentials: authorizationCodeInstalledApp : ${authorizationCodeInstalledApp.receiver}"
//        )
//        return authorizationCodeInstalledApp.authorize("user").setAccessToken("offline")
//    }
//
//    private fun initialize(credential: Credential) {
//        Log.d(TAG, "initialize: Test 3")
//        val transport = NetHttpTransport()
//        mService = Calendar.Builder(transport, JSON_FACTORY, credential)
//            .setApplicationName(APPLICATION_NAME).build()
//    }
//
//    private fun createEvent() {
//        Log.d(TAG, "createEvent: Test")
//        // Create an event
//        val event = Event()
//            .setSummary("Google I/O 2024")
//            .setLocation("800 Howard St., San Francisco, CA 94103")
//            .setDescription("A chance to hear more about Google's developer products.")
//
//        val startDateTime = DateTime("2024-06-28T09:00:00-07:00")
//        val start = EventDateTime()
//            .setDateTime(startDateTime)
//            .setTimeZone("America/Los_Angeles")
//        event.start = start
//
//        val endDateTime = DateTime("2024-06-28T17:00:00-07:00")
//        val end = EventDateTime()
//            .setDateTime(endDateTime)
//            .setTimeZone("America/Los_Angeles")
//        event.end = end
//
//        // Set attendees
//        val attendees = arrayListOf(
//            EventAttendee().setEmail("attendeeEmail@example.com")
//        )
//        event.attendees = attendees
//
//        // Insert the event
//        val calendarId = "primary"
//        val response = mService?.events()?.insert(calendarId, event)?.execute()
//        Log.i(TAG, "Event created: ${response?.htmlLink}")
//    }

}
