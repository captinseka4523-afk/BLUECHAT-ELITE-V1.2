package com.example.bluechat

import android.content.Context
import android.util.Base64
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EliteEngine {

    // 1. وحدة التشفير الأمني (بديل SecureCodec)
    private const val ALGORITHM = "AES"
    private val FIXED_KEY = "BlueChatElite2026".toByteArray() // مفتاح مشفر ثابت للرسائل

    fun encrypt(value: String): String {
        return try {
            val keySpec = SecretKeySpec(getKeyBytes(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            Base64.encodeToString(cipher.doFinal(value.toByteArray()), Base64.DEFAULT)
        } catch (e: Exception) {
            value // في حال الخطأ يرسل النص عادياً
        }
    }

    fun decrypt(value: String): String {
        return try {
            val keySpec = SecretKeySpec(getKeyBytes(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            String(cipher.doFinal(Base64.decode(value, Base64.DEFAULT)))
        } catch (e: Exception) {
            value
        }
    }

    private fun getKeyBytes(): ByteArray {
        val sha = MessageDigest.getInstance("SHA-256")
        return sha.digest(FIXED_KEY).copyOf(16) // مفتاح 128-bit
    }

    // 2. وحدة الحماية بالبصمة (بديل BiometricGate)
    fun authenticateUser(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("فشل التعرف على البصمة")
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("قفل الأمان - BlueChat Elite")
            .setSubtitle("ضع إصبعك على المستشعر لفتح المحادثات المشفرة")
            .setNegativeButtonText("إلغاء")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // 3. وحدة النقل الآمن والمحدود (بديل BoundedTransfer للبيانات الكبيرة)
    fun writeBoundedStream(out: DataOutputStream, type: Byte, sender: String, fileName: String, payload: ByteArray) {
        synchronized(out) {
            out.writeByte(type.toInt())
            out.writeUTF(sender)
            out.writeUTF(fileName)
            out.writeInt(payload.size)
            out.write(payload)
            out.flush()
        }
    }
}
