/*
 * ============LICENSE_START======================================================================
 * Copyright (C) 2018 Nordix Foundation. All rights reserved.
 * ===============================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END========================================================================
 */

package org.onap.dcaegen2.collectors.datafile.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class KeyStoreWrapper implements IKeyStore {
    private KeyStore keyStore;

    public KeyStoreWrapper() throws KeyStoreException {
        keyStore = KeyStore.getInstance("JKS");
    }

    @Override
    public void load(InputStream stream, char[] password)
            throws KeyStoreLoadException {
        try {
            keyStore.load(stream, password);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new KeyStoreLoadException(e);
        }
    }

    @Override
    public KeyStore getKeyStore() {
        return keyStore;
    }

}
