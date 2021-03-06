/*
 * Copyright (C) 2017 The Corona-IDE@github.com Authors
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.starchartlabs.lockdown;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Objects;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.starchartlabs.lockdown.model.KeyFiles;

/**
 * Represents facilities to create key pairs for use in encryption and decryption of credential data
 *
 * @author romeara
 * @since 1.0.0
 */
public class KeyGenerator {

    /**
     * Creates a new key generation instance, initializing any necessary JVM state
     *
     * <p>
     * This implementation utilizes the bouncy castle library, and registers the {@link BouncyCastleProvider}
     */
    public KeyGenerator() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Create a public/private pair of key files, which can be used for encryption and decryption of data
     *
     * @param publicKeyDestination
     *            The location to write the public key to
     * @param privateKeyDestination
     *            The location to write the private key to
     * @return A record of the output locations of the public and private key files
     * @throws IOException
     *             If there is an error writing to the locations specified
     * @since 0.1.0
     */
    public KeyFiles createKeyPair(Path publicKeyDestination, Path privateKeyDestination) throws IOException {
        Objects.requireNonNull(publicKeyDestination);
        Objects.requireNonNull(privateKeyDestination);

        KeyPairGenerator generator = null;

        try {
            generator = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
            generator.initialize(1024, SecureRandom.getInstanceStrong());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            // We register the provider at construction, and RSA is a standard algorithm - if these exceptions occur,
            // something is legitimately wrong with the JVM
            throw new IllegalStateException("Error configuring key generator", e);
        }

        KeyPair pair = generator.generateKeyPair();

        PemObject publicPem = new PemObject("RSA PUBLIC KEY", pair.getPublic().getEncoded());
        PemObject privatePem = new PemObject("RSA PRIVATE KEY", pair.getPrivate().getEncoded());

        try (PemWriter pemWriter = new PemWriter(
                new OutputStreamWriter(Files.newOutputStream(publicKeyDestination, StandardOpenOption.CREATE_NEW)))) {
            pemWriter.writeObject(publicPem);
        }

        try (PemWriter pemWriter = new PemWriter(
                new OutputStreamWriter(Files.newOutputStream(privateKeyDestination, StandardOpenOption.CREATE_NEW)))) {
            pemWriter.writeObject(privatePem);
        }

        return new KeyFiles(publicKeyDestination, privateKeyDestination);
    }

}
