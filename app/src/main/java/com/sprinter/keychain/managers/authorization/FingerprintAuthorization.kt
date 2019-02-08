package com.sprinter.keychain.managers.authorization

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresPermission
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import com.sprinter.keychain.utils.CipherHelper

@TargetApi(Build.VERSION_CODES.M)
class FingerprintAuthorization(context: Context) : AbstractAuthorization(context) {

    override fun destroy() {
    }

    private val mCancellationSignal: CancellationSignal
    private val mFingerprintCallback: FingerprintAuthorizationCallback

    override val type: Int
        @AuthorizationManager.AuthorizationType get() = AuthorizationManager.AUTHORIZATION_STRATEGY_FINGERPRINT

    override val isAvailable: Boolean
        @RequiresPermission(android.Manifest.permission.USE_FINGERPRINT) get() = checkSensorState(
                context) == SensorState.READY && CipherHelper.instance.isReady

    private enum class SensorState {
        NOT_SUPPORTED,
        NOT_BLOCKED, // If the device is not protected by a pin, pattern or password
        NO_FINGERPRINTS, // If there are no prints on the device
        READY
    }

    init {
        mFingerprintCallback = FingerprintAuthorizationCallback()
        mCancellationSignal = CancellationSignal()
        if (!CipherHelper.instance.isAliasExist(KEY_ALIAS)) {
            CipherHelper.instance.generateKeyPairAesStrong(KEY_ALIAS)
        }
        CipherHelper.instance.prepareEncryptAes(KEY_ALIAS)
    }

    @RequiresPermission(android.Manifest.permission.USE_FINGERPRINT) override fun signIn(
            bundle: Bundle) {
        if (!CipherHelper.instance.isAliasExist(KEY_ALIAS)) {
            sendResultFail()
            return
        }

        val cryptoObject = FingerprintManagerCompat.CryptoObject(
                CipherHelper.instance.getCipherAesFingerprint())

        val manager = FingerprintManagerCompat.from(context)
        manager.authenticate(cryptoObject, 0, mCancellationSignal, mFingerprintCallback, null)
    }

    override fun hasAuthData(): Boolean {
        return isAvailable
    }

    override fun restoreAuthorization() {
        throw UnsupportedOperationException("method not implemented")
    }

    override fun logout() {
        mCancellationSignal.cancel()
        super.logout()
    }

    @RequiresPermission(
            android.Manifest.permission.USE_FINGERPRINT) private fun checkFingerprintCompatibility(
            context: Context): Boolean {
        return FingerprintManagerCompat.from(context).isHardwareDetected
    }

    @RequiresPermission(android.Manifest.permission.USE_FINGERPRINT) private fun checkSensorState(
            context: Context): SensorState {
        if (checkFingerprintCompatibility(context)) {
            val keyguardManager = context.getSystemService(
                    Context.KEYGUARD_SERVICE) as KeyguardManager
            if (!keyguardManager.isKeyguardSecure) {
                return SensorState.NOT_BLOCKED
            }

            val fingerprintManager = FingerprintManagerCompat.from(context)

            return if (fingerprintManager.hasEnrolledFingerprints()) SensorState.READY else SensorState.NO_FINGERPRINTS

        } else {
            return SensorState.NOT_SUPPORTED
        }
    }

    private inner class FingerprintAuthorizationCallback :
            FingerprintManagerCompat.AuthenticationCallback() {

        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
            sendResultFail(FingerprintMessage(errMsgId, errString!!.toString()).toBundle())
        }

        override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
            sendResultFail(FingerprintMessage(helpMsgId, helpString!!.toString()).toBundle())
        }

        override fun onAuthenticationSucceeded(
                result: FingerprintManagerCompat.AuthenticationResult?) {
            sendResultSuccess()
        }

        override fun onAuthenticationFailed() {
            sendResultFail()
        }

    }

    class FingerprintMessage(private val error: Int, private val message: String) {

        fun toBundle(): Bundle {
            val result = Bundle();
            result.putInt(KEY_ERROR, error)
            result.putString(KEY_MESSAGE, message)

            return result;
        }

        companion object {

            internal const val KEY_ERROR = "ERROR"
            internal const val KEY_MESSAGE = "MESSAGE"

        }

    }

    companion object {

        private const val KEY_ALIAS = "com.sprinter.keychain.fingerprint"

        fun fromBundle(bundle: Bundle): FingerprintMessage {
            val msgId = bundle.getInt(FingerprintMessage.KEY_ERROR)
            val msg = bundle.getString(FingerprintMessage.KEY_MESSAGE)

            return FingerprintMessage(msgId, msg)
        }

    }

}
