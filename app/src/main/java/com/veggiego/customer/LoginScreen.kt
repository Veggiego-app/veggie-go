package com.veggiego.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.FirebaseException
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import com.google.firebase.firestore.FieldValue

@Composable
fun LoginScreen(navController: NavController) {

    var phone by rememberSaveable {
        mutableStateOf("")
    }
    val context = LocalContext.current

    val auth =
        FirebaseAuth.getInstance()

    var otp by rememberSaveable {
        mutableStateOf("")
    }

    var verificationId by rememberSaveable {
        mutableStateOf("")
    }

    var otpSent by rememberSaveable {
        mutableStateOf(false)
    }
    fun sendOtp() {

        if (phone.length != 10) {

            Toast.makeText(

                context,

                "Enter valid 10 digit mobile number",

                Toast.LENGTH_SHORT

            ).show()

            return

        }

        val options =

            PhoneAuthOptions
                .newBuilder(auth)

                .setPhoneNumber(
                    "+91$phone"
                )

                .setTimeout(
                    60L,
                    TimeUnit.SECONDS
                )

                .setActivity(
                    context as MainActivity
                )

                .setCallbacks(

                    object :
                        PhoneAuthProvider
                        .OnVerificationStateChangedCallbacks() {

                        override fun onVerificationCompleted(
                            credential: PhoneAuthCredential
                        ) {
                        }

                        override fun onVerificationFailed(
                            e: FirebaseException
                        ) {

                            Toast.makeText(

                                context,

                                e.message,

                                Toast.LENGTH_LONG

                            ).show()

                        }

                        override fun onCodeSent(

                            id: String,

                            token:
                            PhoneAuthProvider
                            .ForceResendingToken

                        ) {

                            verificationId = id

                            Toast.makeText(

                                context,

                                "OTP Sent",

                                Toast.LENGTH_SHORT

                            ).show()

                        }

                    }

                )

                .build()

        otpSent = true

        PhoneAuthProvider.verifyPhoneNumber(
            options
        )

    }
    fun verifyOtp() {

        if (otp.length != 6) {

            Toast.makeText(

                context,

                "Enter valid 6 digit OTP",

                Toast.LENGTH_SHORT

            ).show()

            return

        }

        val credential =

            PhoneAuthProvider
                .getCredential(
                    verificationId,
                    otp
                )

        auth.signInWithCredential(
            credential
        )

            .addOnSuccessListener { result ->

                val uid =
                    result.user?.uid ?: ""

                val db =
                    FirebaseFirestore.getInstance()

                val userData =
                    hashMapOf(

                        "uid" to uid,

                        "phone" to "+91$phone",

                        "status" to "ACTIVE",

                        "createdAt" to FieldValue.serverTimestamp(),

                        "joinedDate" to
                                System.currentTimeMillis(),

                        "lastLogin" to
                                System.currentTimeMillis()

                    )

                db.collection("users")
                    .document(uid)
                    .set(
                        userData,
                        SetOptions.merge()
                    )

                    .addOnSuccessListener {

                        Toast.makeText(
                            context,
                            "Login Success",
                            Toast.LENGTH_SHORT
                        ).show()

                        navController.navigate("home") {

                            popUpTo("login") {
                                inclusive = true
                            }

                        }

                    }

                    .addOnFailureListener {

                        Toast.makeText(
                            context,
                            "Login Success",
                            Toast.LENGTH_SHORT
                        ).show()

                        navController.navigate("home") {

                            popUpTo("login") {
                                inclusive = true
                            }

                        }

                    }

            }

            .addOnFailureListener {

                Toast.makeText(

                    context,

                    "Invalid OTP",

                    Toast.LENGTH_SHORT

                ).show()

            }

    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Column(

            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .imePadding()
                .navigationBarsPadding()
                .padding(24.dp),

            verticalArrangement = Arrangement.Top,

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(70.dp)
            )

            Text(
                text = "VeggieGo",

                fontSize = 55.sp,

                fontWeight = FontWeight.ExtraBold,

                color = Color(0xFF2E7D32)

            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(

                text = "Pure Veg Food Delivery",

                fontSize = 20.sp,

                color = Color.Black

            )
            if (otpSent) {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "Enter OTP",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Enter the verification code sent to +91$phone",
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (!otpSent) {
                Text(

                    text = "Welcome Back 👋",

                    fontSize = 24.sp,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFF1B5E20)

                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(

                    text =

                        "Order delicious pure veg food\nin Gandhidham & Adipur",

                    fontSize = 18.sp,

                    color = Color.Black

                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )
                OutlinedTextField(

                    value = phone,

                    onValueChange = {

                        phone =

                            it.filter { c ->

                                c.isDigit()

                            }.take(10)

                    },

                    keyboardOptions =

                        KeyboardOptions(

                            keyboardType =
                                KeyboardType.Phone,

                            imeAction =
                                ImeAction.Send

                        ),

                    keyboardActions =

                        KeyboardActions(

                            onSend = {

                                sendOtp()

                            }

                        ),

                    label = {
                        Text("Mobile Number (10 digits)")
                    },

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!otpSent) {

                Button(

                    onClick = {

                        sendOtp()

                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF2E7D32)
                        )

                ) {

                    Text(
                        "Send OTP"
                    )
                }

            } else {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                OutlinedTextField(

                    value = otp,

                    onValueChange = {

                        otp =

                            it.filter { c ->

                                c.isDigit()

                            }.take(6)

                    },

                    keyboardOptions =

                        KeyboardOptions(

                            keyboardType =
                                KeyboardType.NumberPassword,

                            imeAction =
                                ImeAction.Done

                        ),

                    keyboardActions =

                        KeyboardActions(

                            onDone = {

                                verifyOtp()

                            }

                        ),

                    label = {
                        Text("Enter OTP (6 digits)")
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                )
                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween

                ) {

                    TextButton(

                        onClick = {

                            otpSent = false

                            otp = ""

                            verificationId = ""

                        }

                    ) {

                        Text("Change Number")

                    }

                    TextButton(

                        onClick = {

                            sendOtp()

                        }

                    ) {

                        Text("Resend OTP")

                    }

                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(

                    onClick = {

                        verifyOtp()

                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF2E7D32)
                        )

                ) {

                    Text(
                        "Verify OTP"
                    )
                }
            }
        }
    }
}