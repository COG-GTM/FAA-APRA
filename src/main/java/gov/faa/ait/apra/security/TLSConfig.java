/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import java.security.Security;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STIG V-220634 (NIST SC-8): TLS configuration enforcement.
 * Ensures only TLS 1.2+ is used for all communications.
 * SSLv2, SSLv3, TLS 1.0, and TLS 1.1 are disabled.
 */
public final class TLSConfig {

    private static final Logger logger = LoggerFactory.getLogger(TLSConfig.class);
    private static volatile boolean configured = false;

    private TLSConfig() { }

    /**
     * Configure JVM-wide TLS settings to enforce TLS 1.2+ only.
     * Should be called during application startup.
     */
    public static synchronized void enforceMinimumTLS() {
        if (configured) {
            return;
        }
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");
        String required = "SSLv3, TLSv1, TLSv1.1, RC4, DES, MD5withRSA, DH keySize < 1024, "
            + "EC keySize < 224, 3DES_EDE_CBC, anon, NULL";
        String existing = Security.getProperty("jdk.tls.disabledAlgorithms");
        if (existing != null && !existing.isEmpty()) {
            Security.setProperty("jdk.tls.disabledAlgorithms", existing + ", " + required);
        } else {
            Security.setProperty("jdk.tls.disabledAlgorithms", required);
        }

        try {
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, null, null);
            SSLContext.setDefault(context);
            logger.info("TLS configuration enforced: TLS 1.2+ only (STIG V-220634)");
        } catch (Exception e) {
            logger.error("Failed to enforce TLS 1.2+ configuration", e);
        }
        configured = true;
    }
}
