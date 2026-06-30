package dev.fzer0x.fucksolidexplorer

import android.content.ContentResolver
import android.os.Build
import android.provider.Settings
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import java.net.UnknownHostException
import java.util.UUID

class XposedInit : XposedModule() {

    companion object {
        private const val TAG = "FuckSolidExplorer"
        private const val TARGET_PACKAGE = "pl.solidexplorer2"
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        super.onPackageLoaded(param)
        if (param.packageName != TARGET_PACKAGE) return
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        super.onPackageReady(param)
        if (param.packageName != TARGET_PACKAGE) return

        val sessionUuid = UUID.randomUUID().toString()
        val fakeAndroidId = sessionUuid.replace("-", "").substring(0, 16)
        
        log(android.util.Log.INFO, TAG, "Hooking into pl.solidexplorer2")

        try {
            val licenseClass = "pl.solidexplorer.licensing.SELicenseManager"
            val clazz = Class.forName(licenseClass, true, param.classLoader)
            clazz.declaredMethods.forEach { method ->
                val name = method.name.lowercase()
                if (method.returnType == Boolean::class.java || method.returnType == java.lang.Boolean.TYPE) {
                    if (name.contains("license") || name.contains("premium") || name.contains("unlocked") || 
                        name.contains("fullversion") || name.contains("valid")) {
                        hook(method).intercept { true }
                    } else if (name.contains("trial") || name.contains("expired") || name.contains("ads")) {
                        hook(method).intercept { false }
                    }
                }
            }
        } catch (e: Throwable) {
            log(android.util.Log.ERROR, TAG, "Licensing hook failed: ${e.message}")
        }

        try {
            val settingsSecure = Settings.Secure::class.java
            val getStringMethod = settingsSecure.getDeclaredMethod("getString", ContentResolver::class.java, String::class.java)
            hook(getStringMethod).intercept { chain ->
                if (chain.getArg(1) == Settings.Secure.ANDROID_ID) {
                    fakeAndroidId
                } else {
                    chain.proceed()
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val getSerialMethod = Build::class.java.getDeclaredMethod("getSerial")
                hook(getSerialMethod).intercept { fakeAndroidId }
            }
            
            try {
                val serialField = Build::class.java.getDeclaredField("SERIAL")
                serialField.isAccessible = true
                serialField.set(null, fakeAndroidId)
            } catch (ignored: Throwable) {}
        } catch (ignored: Throwable) {}

        try {
            val inetAddressClass = Class.forName("java.net.InetAddress")
            val getAllByNameMethod = inetAddressClass.getDeclaredMethod("getAllByName", String::class.java)
            hook(getAllByNameMethod).intercept { chain ->
                val host = chain.getArg(0) as? String ?: return@intercept chain.proceed()
                val blocked = listOf("adservice", "googleads", "doubleclick", "firebase", "crashlytics")
                if (blocked.any { host.contains(it) }) {
                    throw UnknownHostException("Blocked")
                }
                chain.proceed()
            }
        } catch (ignored: Throwable) {}
    }
}
