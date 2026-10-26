package com.example.memotrip_kroniq.data.datastore

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface SessionCipher {
    fun encrypt(plaintext: ByteArray): ByteArray
    fun decrypt(ciphertext: ByteArray): ByteArray
}

class AesGcmSessionCipher(private val key: () -> SecretKey) : SessionCipher {
    private val aad = "MemoTrip.auth.v1".toByteArray(Charsets.UTF_8)
    override fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        cipher.updateAAD(aad)
        return byteArrayOf(1) + cipher.iv + cipher.doFinal(plaintext)
    }
    override fun decrypt(ciphertext: ByteArray): ByteArray {
        require(ciphertext.size >= 29 && ciphertext[0] == 1.toByte()) { "Invalid secure storage format" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, ciphertext.copyOfRange(1, 13)))
        cipher.updateAAD(aad)
        return cipher.doFinal(ciphertext.copyOfRange(13, ciphertext.size))
    }
}
