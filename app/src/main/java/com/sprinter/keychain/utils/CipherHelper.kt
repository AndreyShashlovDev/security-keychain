package com.sprinter.keychain.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.Size
import android.support.annotation.WorkerThread
import android.text.TextUtils
import android.util.Base64
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.nio.charset.StandardCharsets.UTF_8
import java.security.*
import java.security.cert.CertificateException
import java.security.interfaces.RSAPublicKey
import java.security.spec.MGF1ParameterSpec
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal


class CipherHelper private constructor() {

    private lateinit var keyStore: KeyStore
    private lateinit var cipherRsa: Cipher
    private lateinit var cipherAesFingerprint: Cipher

    private lateinit var cipherAes: Cipher
    private lateinit var preferences: SharedPreferences
    private lateinit var specSha512: OAEPParameterSpec
    var isReady: Boolean = false

    private val supportRsaAlgorithm: String
        get() = if (needUseOldApi()) ALGORITHM_RSA_API_18 else ALGORITHM_RSA_API_23_STRONG

    private enum class CipherHelperSingleton {
        INSTANCE;

        internal var instance: CipherHelper = CipherHelper()
    }

    init {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            cipherRsa = Cipher.getInstance(supportRsaAlgorithm)
            if (!needUseOldApi()) {
                cipherAesFingerprint = Cipher.getInstance(ALGORITHM_AES_WITH_PARAMS)
            }

            specSha512 = OAEPParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA1,
                    PSource.PSpecified.DEFAULT)

            cipherAes = Cipher.getInstance(ALGORITHM_AES_WITH_PARAMS)
            isReady = true

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun initializePreferences(context: Context) {
        preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(
                context.applicationContext)
    }

    fun getCipherAesFingerprint(): Cipher {
        return cipherAesFingerprint
    }

    fun prepareEncryptAes(alias: String) {
        val aesKeystoreAlias = alias + ALGORITHM_AES_WITH_PARAMS
        if (isAliasExist(alias) && keyStore.entryInstanceOf(aesKeystoreAlias,
                KeyStore.SecretKeyEntry::class.java)) {
            val publicKey = keyStore.getKey(aesKeystoreAlias, null) as SecretKey
            cipherAesFingerprint.init(Cipher.ENCRYPT_MODE, publicKey)
        }
    }

    private fun needUseOldApi(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M
    }

    fun isAliasExist(alias: String): Boolean {
        try {
            return keyStore.containsAlias(alias) || keyStore.containsAlias(
                    alias + ALGORITHM_AES_WITH_PARAMS)
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }

        return false
    }

    fun deleteAlias(alias: String) {
        try {
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }
    }

    @WorkerThread
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun generateKeyPairAesStrong(alias: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            generateKeyPairAesStrongWithKeystoreApiM(alias + ALGORITHM_AES_WITH_PARAMS)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            generateKeyPairAesStrongApiKitKat(alias)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @WorkerThread private fun generateKeyPairAesStrongWithKeystoreApiM(alias: String) {
        try {
            existAlias(alias)
            val serialNumber = BigInteger(18, Random())

            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)

            val spec = KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setCertificateSubject(
                    X500Principal("CN=$alias, O=Android Authority AES")).setCertificateSerialNumber(
                    serialNumber).setKeySize(ALGORITHM_AES_KEY_SIZE).setBlockModes(
                    KeyProperties.BLOCK_MODE_CBC).setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_PKCS7).setRandomizedEncryptionRequired(
                    false).setUserAuthenticationRequired(false).build()
            keyGen.init(spec)
            keyGen.generateKey()

        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @WorkerThread private fun generateKeyPairAesStrongApiKitKat(alias: String) {
        try {
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM_AES_WITHOUT_PARAMS)
            val secureRandom = SecureRandom.getInstance(ALGORITHM_SHA1PRNG)
            secureRandom.setSeed(SecureRandom.getSeed(1000))

            keyGenerator.init(ALGORITHM_AES_KEY_SIZE, secureRandom)

            val key = keyGenerator.generateKey()
            val keyBase64 = Base64.encodeToString(key.encoded, Base64.NO_WRAP)
            val encryptedKey = rsaKeyStoreEncrypt(alias, keyBase64)
            preferences.edit().putString(PREF_AES_KEY, encryptedKey).apply()

        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @WorkerThread
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun generateKeyPairRsaStrongWithKeystore(alias: String, context: Context): KeyPair? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return generateKeyPairRsaStrongWithKeystoreM(alias, false)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return generateKeyPairRsaStrongWithKeystoreKitKat(alias, context)
        }

        return null
    }

    @TargetApi(Build.VERSION_CODES.M) private fun generateKeyPairRsaStrongWithKeystoreM(
            alias: String, requireAuth: Boolean): KeyPair? {
        try {
            existAlias(alias)

            val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA,
                    ANDROID_KEYSTORE)
            keyPairGenerator.initialize(KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(
                    KeyProperties.BLOCK_MODE_ECB).setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_RSA_OAEP).setKeySize(
                    ALGORITHM_RSA_3072_KEY_SIZE).setDigests(KeyProperties.DIGEST_SHA256,
                    KeyProperties.DIGEST_SHA384,
                    KeyProperties.DIGEST_SHA512).setUserAuthenticationRequired(requireAuth).build())

            return keyPairGenerator.generateKeyPair()

        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    @TargetApi(Build.VERSION_CODES.KITKAT) private fun generateKeyPairRsaStrongWithKeystoreKitKat(
            alias: String, context: Context): KeyPair? {
        try {
            existAlias(alias)

            val start = Calendar.getInstance()
            start.timeInMillis = System.currentTimeMillis()
            val end = Calendar.getInstance()

            end.add(Calendar.YEAR, 100)

            val serialNumber = BigInteger(18, Random())

            val spec = KeyPairGeneratorSpec.Builder(context).setAlias(alias).setSerialNumber(
                    serialNumber).setStartDate(start.time).setEndDate(end.time).setSubject(
                    X500Principal("CN=$alias, O=Android Authority")).setKeySize(
                    ALGORITHM_RSA_3072_KEY_SIZE).build()
            val generator = KeyPairGenerator.getInstance(ALGORITHM_RSA, ANDROID_KEYSTORE)
            generator.initialize(spec)

            return generator.generateKeyPair()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class,
            IOException::class, IllegalArgumentException::class) private fun existAlias(
            alias: String) {
        if (keyStore.containsAlias(alias)) {
            throw IllegalArgumentException("Alias already exist!")
        }
    }

    fun aesKeyStoreEncrypt(alias: String, originMessage: String): String? {
        try {
            val ivParams = getAesIvSpec(alias)

            if (ivParams != null) {
                cipherAes.init(Cipher.ENCRYPT_MODE, getAesKey(alias), ivParams)
            } else {
                cipherAes.init(Cipher.ENCRYPT_MODE, getAesKey(alias))
            }

            val encryptedBytes = cipherAes.doFinal(originMessage.toByteArray(UTF_8))

            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: UnrecoverableEntryException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }

        return null
    }

    fun aesKeyStoreDecrypt(alias: String, base64Message: String): String? {
        try {
            val originEncodeMessage = Base64.decode(base64Message, Base64.NO_WRAP)
            cipherAes.init(Cipher.DECRYPT_MODE, getAesKey(alias), getAesIvSpec(alias))
            val encryptedBytes = cipherAes.doFinal(originEncodeMessage)

            return if (encryptedBytes == null) null else String(encryptedBytes, UTF_8)

        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: UnrecoverableEntryException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }

        return null
    }

    private fun saveAesIvSpec(alias: String) {
        aesKeyStoreEncrypt(alias, "initialize params")

        val iv = getAesIvSpec(alias)?.iv
        iv ?: throw IllegalStateException("cipher Iv not initialized!")

        if (TextUtils.isEmpty(preferences.getString(PREF_KEY_IV, ""))) {
            val paramsBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encodeParams = rsaKeyStoreEncrypt(alias, paramsBase64)
            preferences.edit().putString(PREF_KEY_IV, encodeParams).apply()
        }
    }

    private fun getAesIvSpec(alias: String): IvParameterSpec? {
        val encryptedParams: String? = preferences.getString(PREF_KEY_IV, "")
        val params: ByteArray
        if (!TextUtils.isEmpty(encryptedParams)) {
            val paramsBase64 = rsaKeyStoreDecrypt(alias, encryptedParams!!)
            params = Base64.decode(paramsBase64, Base64.NO_WRAP)

        } else if (cipherAes.iv != null) {
            params = cipherAes.iv

        } else {
            if (needUseOldApi()) {
                val secureRandom = SecureRandom()
                val ivBytes = ByteArray(IV_BYTE_LEN)
                secureRandom.nextBytes(ivBytes)

                params = ivBytes
            } else {
                return null
            }
        }

        return IvParameterSpec(params)
    }

    private fun getAesKey(alias: String): SecretKey {
        if (needUseOldApi()) {
            val encryptedKey = preferences.getString(PREF_AES_KEY, null)
            if (TextUtils.isEmpty(encryptedKey)) {
                throw IllegalStateException(
                        "need generate secret key. call CipherHelper#initializeKeystore")
            } else {
                val decryptedBase64 = rsaKeyStoreDecrypt(alias, encryptedKey)
                val key = Base64.decode(decryptedBase64, Base64.NO_WRAP)

                return SecretKeySpec(key, ALGORITHM_AES_WITHOUT_PARAMS);
            }
        } else {
            val aesKeystoreAlias = alias + ALGORITHM_AES_WITH_PARAMS
            if (isAliasExist(alias) && keyStore.entryInstanceOf(aesKeystoreAlias,
                    KeyStore.SecretKeyEntry::class.java)) {

                return keyStore.getKey(aesKeystoreAlias, null) as SecretKey
            } else {
                throw IllegalStateException(
                        "need generate secret key. call CipherHelper#initializeKeystore")
            }
        }
    }

    fun rsaKeyStoreEncrypt(alias: String, message: String): String? {
        try {
            val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
            val publicKey = privateKeyEntry.certificate.publicKey as RSAPublicKey
            if (needUseOldApi()) {
                cipherRsa.init(Cipher.ENCRYPT_MODE, publicKey)

            } else {
                cipherRsa.init(Cipher.ENCRYPT_MODE, publicKey, specSha512)
            }

            val encryptedBytes = cipherRsa.doFinal(message.toByteArray(UTF_8))

            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnrecoverableEntryException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }

        return null
    }

    fun rsaKeyStoreDecrypt(alias: String, message: String): String? {
        try {
            val originEncodeMessage = Base64.decode(message, Base64.NO_WRAP)
            val privateKey = keyStore.getKey(alias, null) as PrivateKey
            cipherRsa.init(Cipher.DECRYPT_MODE, privateKey)
            val decryptedBytes = cipherRsa.doFinal(originEncodeMessage)

            return String(decryptedBytes, UTF_8)

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnrecoverableEntryException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }

        return null
    }

    companion object {

        const val ALGORITHM_RSA = "RSA"
        const val ALGORITHM_AES_WITHOUT_PARAMS = "AES"
        const val ALGORITHM_SHA1PRNG = "SHA1PRNG"
        const val ALGORITHM_SHA_256 = "SHA-256"
        const val ALGORITHM_AES_WITH_PARAMS = "AES/CBC/PKCS7Padding"
        const val ALGORITHM_RSA_API_18 = "RSA/ECB/PKCS1Padding" // for api 10+
        const val ALGORITHM_RSA_API_23_STRONG = "RSA/ECB/OAEPwithSHA-512andMGF1Padding" //for api 23+
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val PREF_KEY_IV = "PREF_KEY_IV"
        const val PREF_AES_KEY = "PREF_AES_KEY"
        const val ALGORITHM_RSA_4096_KEY_SIZE = 4096L
        const val ALGORITHM_RSA_3072_KEY_SIZE = 3072
        const val ALGORITHM_AES_KEY_SIZE = 256
        const val IV_BYTE_LEN = 16

        internal val instance: CipherHelper
            @TargetApi(Build.VERSION_CODES.KITKAT) get() = CipherHelperSingleton.INSTANCE.instance

        fun generateSha256x2Base64(value: String): String {
            val bytes = generateSha256x2(value)
            return Base64.encodeToString(bytes, Base64.NO_WRAP)
        }

        @Size(32)
        fun generateSha256x2(value: String): ByteArray {
            return generateSha256(generateSha256(value.toByteArray()))
        }

        @Size(32)
        fun generateSha256(value: ByteArray): ByteArray {
            try {
                val digest = MessageDigest.getInstance(ALGORITHM_SHA_256)
                digest.update(value)
                val bytes = digest.digest()
                digest.update(bytes)

                return digest.digest()

            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }

        }

        @WorkerThread
        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun initializeKeystore(alias: String, context: Context) {
            instance.initializePreferences(context)

            if (instance.isAliasExist(alias)) {
                return
            }
            Thread.sleep(1000) //dirty hack. create androidKeyStore freeze app even in worker thread
            instance.generateKeyPairRsaStrongWithKeystore(alias, context)

            instance.generateKeyPairAesStrong(alias)
            instance.saveAesIvSpec(alias)
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun encryptAesByAndroidVersion(alias: String, originText: String): String? {
            return instance.aesKeyStoreEncrypt(alias, originText)
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun decryptAesByAndroidVersion(alias: String, base64Text: String): String? {
            return instance.aesKeyStoreDecrypt(alias, base64Text)
        }

    }

}
