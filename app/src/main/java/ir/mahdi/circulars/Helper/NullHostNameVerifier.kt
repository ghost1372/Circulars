package ir.mahdi.circulars.Helper

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

// this is for fixing SSL Certificate bypass
class NullHostNameVerifier : HostnameVerifier {
    override fun verify(p0: String?, p1: SSLSession?): Boolean {
        return true
    }
}