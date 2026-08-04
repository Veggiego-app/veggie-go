package com.veggiego.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ChatScreen(

    navController: NavController,

    orderId: String
) {

    val currentUser =

        FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid ?: ""

    var messageText by remember {

        mutableStateOf("")
    }

    val messages = remember {

        mutableStateListOf<ChatMessage>()
    }

    val listState =
        rememberLazyListState()

    val coroutineScope =
        rememberCoroutineScope()

    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()
            .collection("chats")
            .document(orderId)
            .collection("messages")
            .orderBy("timestamp")

            .addSnapshotListener { value, _ ->

                messages.clear()

                value?.documents?.forEach {

                    val msg =

                        it.toObject(
                            ChatMessage::class.java
                        )

                    if (msg != null) {

                        messages.add(msg)

                        coroutineScope.launch {

                            if (messages.isNotEmpty()) {

                                listState.animateScrollToItem(
                                    messages.size - 1
                                )
                            }
                        }
                    }
                }
            }
    }

    LaunchedEffect(messages.size) {

        if (messages.isNotEmpty()) {

            listState.animateScrollToItem(
                messages.size - 1
            )
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
    ) {

        TopAppBar(

            title = {

                Text(
                    "💬 Live Chat"
                )
            }
        )

        LazyColumn(

            state = listState,

            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),

            contentPadding =
                PaddingValues(
                    top = 12.dp,
                    bottom = 6.dp
                )
        ) {

            items(messages) { msg ->

                if (

                    msg.senderType == "rider"

                    &&

                    !msg.seen

                    &&

                    msg.id.isNotEmpty()

                ) {

                    FirebaseFirestore
                        .getInstance()
                        .collection("chats")
                        .document(orderId)
                        .collection("messages")
                        .document(msg.id)

                        .update(
                            "seen",
                            true
                        )
                }

                val isMine =

                    msg.senderId ==
                            currentUser

                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),

                    horizontalArrangement =

                        if (isMine)

                            Arrangement.End

                        else

                            Arrangement.Start
                ) {

                    Column(

                        horizontalAlignment =

                            if (isMine)

                                Alignment.End

                            else

                                Alignment.Start
                    ) {

                        Card(

                            colors =
                                CardDefaults.cardColors(

                                    containerColor =

                                        if (isMine)

                                            Color(
                                                0xFF00C853
                                            )

                                        else

                                            Color.LightGray
                                ),

                            shape =
                                RoundedCornerShape(
                                    18.dp
                                )
                        ) {

                            Text(

                                text =
                                    msg.message,

                                modifier =
                                    Modifier.padding(
                                        14.dp
                                    ),

                                color =

                                    if (isMine)

                                        Color.White

                                    else

                                        Color.Black,

                                fontSize =
                                    16.sp
                            )
                        }

                        if (isMine) {

                            Spacer(
                                modifier =
                                    Modifier.height(2.dp)
                            )

                            Text(

                                text =

                                    if (msg.seen)

                                        "✔✔ Seen"

                                    else

                                        "✔ Sent",

                                fontSize = 11.sp,

                                color =

                                    if (msg.seen)

                                        Color(
                                            0xFF2962FF
                                        )

                                    else

                                        Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 4.dp,
                        bottom = 4.dp
                    )
                    .navigationBarsPadding(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            OutlinedTextField(

                value = messageText,

                onValueChange = {

                    messageText = it
                },

                modifier =
                    Modifier.weight(1f),

                placeholder = {

                    Text(
                        "Type message..."
                    )
                },

                shape =
                    RoundedCornerShape(
                        30.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            FloatingActionButton(

                onClick = {

                    if (
                        messageText.isNotEmpty()
                    ) {

                        val messageId =

                            FirebaseFirestore
                                .getInstance()
                                .collection("temp")
                                .document()
                                .id

                        val msg =

                            ChatMessage(

                                id = messageId,

                                senderId =
                                    currentUser,

                                senderType =
                                    "customer",

                                message =
                                    messageText,

                                timestamp =
                                    System.currentTimeMillis(),

                                seen = false
                            )

                        FirebaseFirestore
                            .getInstance()
                            .collection("chats")
                            .document(orderId)
                            .collection("messages")
                            .document(messageId)
                            .set(msg)

                        messageText = ""
                    }
                }

            ) {

                Icon(

                    Icons.Default.Send,

                    contentDescription =
                        null
                )
            }
        }
    }
}