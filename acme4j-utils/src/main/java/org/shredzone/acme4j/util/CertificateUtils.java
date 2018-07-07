/*
 * acme4j - Java ACME client
 *
 * Copyright (C) 2015 Richard "Shred" Körber
 *   http://acme4j.shredzone.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.acme4j.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.shredzone.acme4j.Certificate;

/**
 * Utility class offering convenience methods for certificates.
 * <p>
 * Requires {@code Bouncy Castle}. This class is part of the {@code acme4j-utils} module.
 */
public final class CertificateUtils {

    private CertificateUtils() {
        // utility class without constructor
    }

    /**
     * Reads an {@link X509Certificate} PEM file from an {@link InputStream}.
     *
     * @param in
     *            {@link InputStream} to read the certificate from. The
     *            {@link InputStream} is closed after use.
     * @return {@link X509Certificate} that was read
     */
    public static X509Certificate readX509Certificate(InputStream in) throws IOException {
        try (InputStream uin = in) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(uin);
        } catch (CertificateException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Writes an X.509 certificate PEM file.
     *
     * @param cert
     *            {@link X509Certificate} to write
     * @param out
     *            {@link OutputStream} to write the PEM file to. The {@link OutputStream}
     *            is closed after use.
     */
    public static void writeX509Certificate(X509Certificate cert, OutputStream out) throws IOException {
        writeX509Certificate(cert, new OutputStreamWriter(out, "utf-8"));
    }

    /**
     * Writes an X.509 certificate PEM file.
     *
     * @param cert
     *            {@link X509Certificate} to write
     * @param w
     *            {@link Writer} to write the PEM file to. The {@link Writer} is closed
     *            after use.
     */
    public static void writeX509Certificate(X509Certificate cert, Writer w) throws IOException {
        writeX509Certificates(w, cert);
    }

    /**
     * Writes a X.509 certificate chain to a PEM file.
     *
     * @param w
     *            {@link Writer} to write the certificate chain to. The {@link Writer} is
     *            closed after use.
     * @param cert
     *            {@link X509Certificate} to write, {@code null} to skip this certificate
     * @param chain
     *            {@link X509Certificate} chain to add to the certificate. {@code null}
     *            values are ignored, array may be empty.
     * @deprecated use {@link Certificate#downloadFullChain()} and
     *             {@link #writeX509Certificates(Writer, X509Certificate[])} instead
     */
    @Deprecated
    public static void writeX509CertificateChain(Writer w, X509Certificate cert, X509Certificate... chain)
                throws IOException {
        X509Certificate[] certs = new X509Certificate[chain.length + 1];
        certs[0] = cert;
        System.arraycopy(chain, 0, certs, 1, chain.length);
        writeX509Certificates(w, certs);
    }

    /**
     * Writes multiple X.509 certificates to a PEM file.
     *
     * @param w
     *            {@link Writer} to write the certificate chain to. The {@link Writer} is
     *            closed after use.
     * @param certs
     *            {@link X509Certificate} certificates to add to the certificate.
     *            {@code null} values are ignored, array may be empty.
     * @since 1.1
     */
    public static void writeX509Certificates(Writer w, X509Certificate... certs)
                throws IOException {
        try (JcaPEMWriter jw = new JcaPEMWriter(w)) {
            for (X509Certificate c : certs) {
                if (c != null) {
                    jw.writeObject(c);
                }
            }
        }
    }

    /**
     * Reads a CSR PEM file.
     *
     * @param in
     *            {@link InputStream} to read the CSR from. The {@link InputStream} is
     *            closed after use.
     * @return CSR that was read
     */
    public static PKCS10CertificationRequest readCSR(InputStream in) throws IOException {
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(in))) {
            Object parsedObj = pemParser.readObject();
            if (!(parsedObj instanceof PKCS10CertificationRequest)) {
                throw new IOException("Not a PKCS10 CSR");
            }
            return (PKCS10CertificationRequest) parsedObj;
        }
    }

}
