/*
 * Copyright 2014 Attila Szegedi, Daniel Dekany, Jonathan Revusky
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.hub.api.fmpp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/**
 * <code>OutputStream</code> that simply drops what it gets. 
 */
public class NullZipOutputStream extends ZipOutputStream {

    public static final OutputStream out=new ByteArrayOutputStream();
    public static final ZipOutputStream INSTANCE = new NullZipOutputStream(out);

    private NullZipOutputStream(OutputStream out) {
    super(out);
    }

    public void write(int b) throws IOException {
        //nop
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        //nop
    }

}
