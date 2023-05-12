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

package io.apicurio.hub.api.fmpp;

import io.apicurio.hub.api.fmpp.util.FileUtil;
import io.apicurio.hub.api.fmpp.util.NullWriter;
import io.apicurio.hub.api.fmpp.util.NullZipOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The writer that FMPP uses to write file output.
 */
class FmppFileOutputWriter extends FmppOutputWriter {
    private static final int BUFFER_SIZE = 16000; // large buffer slows down
    
    private Engine engine;
    private ArrayList stateStack = new ArrayList();
    private boolean closed = false;
    private boolean ignoreFlush = false;
     // State fields. Keep in sync. with save/restoreState!
    private File dst;
    private String enc;
    private int freeBuf = BUFFER_SIZE;
    private StringBuffer buf = new StringBuffer(BUFFER_SIZE);
    private Writer fileWriter;
  //  private ZipOutputStream zipWriter;
    private ZipOutputStream zipFileWriter;
    private ByteArrayOutputStream baos;
    private ByteArrayOutputStream baosClone;
    private boolean append;
    private SavedState sharedSavedState;

    FmppFileOutputWriter(Engine engine, File dst, String enc) {
        this.engine = engine;
        this.dst = dst;
        this.enc = enc;
        zipFileWriter= NullZipOutputStream.INSTANCE;
        baos=new ByteArrayOutputStream();
        baosClone=new ByteArrayOutputStream();

    }

    FmppFileOutputWriter(Engine engine, ZipOutputStream zos) {
        this.engine = engine;
        //zipFileWriter= NullZipOutputStream.INSTANCE;
        zipFileWriter=zos;
        baos=new ByteArrayOutputStream();
        baosClone=new ByteArrayOutputStream();
        this.enc = "UTF-8";

    }

    public void write(String data) throws IOException {
        if (fileWriter != null) {
            fileWriter.write(data);
        //    zipWriter.write(data.getBytes());
        } else {
            int ln = data.length();
            if (ln <= freeBuf) {
                buf.append(data);
                freeBuf -= ln;
            } else {
                createFileWriter();
                fileWriter.write(buf.toString());
                buf = null;
                fileWriter.write(data);
            //    zipWriter.write(data.getBytes());
            }
        }
    }
    
    public void write(char[] data, int off, int len) throws IOException {
        if (fileWriter != null) {
            fileWriter.write(data, off, len);
         //   zipWriter.write(data.toString().getBytes(),off,len);
        } else {
            int ln = data.length;
            if (ln <= freeBuf) {
                buf.append(data, off, len);
                freeBuf -= ln;
            } else {
                createFileWriter();
                fileWriter.write(buf.toString());
              //  zipWriter.write(buf.toString().getBytes());
                buf = null;
                fileWriter.write(data, off, len);
             //   zipWriter.write(data.toString().getBytes(),off,len);
            }
        }
    }
    
    // Affects current writer only!
    public void flush() throws IOException {
        if (!ignoreFlush) {
            if (fileWriter == null) {
                createFileWriter();
                fileWriter.write(buf.toString());
              //  zipWriter.write(buf.toString().getBytes());
               // zipWriter.closeEntry();
                buf = null;
            }

            fileWriter.flush();
         //   zipFileWriter.write(baos.toByteArray());
          // baosClone.write(baos.toByteArray(), 0, baos.size());
           // zipWriter.closeEntry();
           // zipFileWriter.closeEntry();
        }
    }
    
    // Close *all* writers here!
    public void close(boolean error) throws IOException {
        if (closed) {
            return;
        }
        try {
            do {
                try {
                    if (!error
                            || fileWriter != null
                            || (buf != null && buf.length() != 0)) {
                        flush();
                    }
                } finally {
                    if (fileWriter != null) {
                        fileWriter.close();
                     //   zipWriter.close();
                        }
                }
                fileWriter = null;
               // zipWriter=null;
                if (stateStack.size() == 0) {
                    break;
                }
                nestOutputFileEnd(true);
            } while (true);
        } finally {
            // Final attempt to release resources
            Iterator it = stateStack.iterator();
            while (it.hasNext()) {
                SavedState s = (SavedState) it.next();
                try {
                    s.fileWriter.close();
                 //   s.zipWriter.close();
                } catch (Throwable exc) {
                    //!!logme
                    ; //ignore
                }
            }
            stateStack.clear();
            closed = true;
        }
    }
    
    public void close() throws IOException {
        close(false);
    }

    void dropOutputFile() throws IOException {
        if (fileWriter != null) {
            fileWriter.close();
       //     zipWriter.close();
            if (dst.isFile()) {
                dst.delete();
            }
        }
        fileWriter = NullWriter.INSTANCE;
      //  zipWriter = NullZipOutputStream.INSTANCE;
        buf = null; 
    }
    
    void restartOutputFile() throws IOException {
        if (fileWriter != null) {
            fileWriter.close();
         //   zipWriter.close();
        }
        initOutputBufferAndWriter();
    }

    void renameOutputFile(String newName) throws IOException {
        File newDst = deduceNewDst(newName);
        if (dst.equals(newDst)) {
            return;
        }
        if (fileWriter != null) {
            flush();
            fileWriter.close();
           // zipWriter.close();
            
            initOutputBufferAndWriter();
            
            if (!dst.isFile()) {
                throw new IOException("Can't find the file to rename: "
                        + dst.getPath());
            }
            if (newDst.isFile()) {
                newDst.delete();
            }
            File parent = newDst.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (!dst.renameTo(newDst)) {
                throw new IOException("Failed to rename " + dst.getPath()
                        + " to " + newDst.getPath());
            }
            append = true;
        }
        dst = newDst;
    }
    
    void changeOutputFile(String newName, boolean append)
            throws IOException {
        File newDst = deduceNewDst(newName);
        if (dst.equals(newDst)) {
            return;
        }
 
        for (int i = stateStack.size() - 1; i >= 0; i--) {
            SavedState s = (SavedState) stateStack.get(i);
            if (s.dst.equals(newDst)) {
                if (sharedSavedState == null || s != sharedSavedState) {
                    throw new IOException(
                        "Something is already using this file as output: "
                        + newDst.getAbsolutePath());
                } else {
                    break;
                }
            }
        }

        
        flush();
        fileWriter.close();
       // zipWriter.close();

        initOutputBufferAndWriter();        
        this.append = append;
         
        dst = newDst;
    }

    void nestOutputFileBegin(String newName, boolean append)
            throws IOException {
        File newDst = deduceNewDst(newName);

        SavedState s = new SavedState();
        s.store();
        stateStack.add(s);

        for (int i = stateStack.size() - 1; i >= 0; i--) {
            s = (SavedState) stateStack.get(i);
            if (s.dst.equals(newDst)) {
                s.load();
                // intentionally ignore "append" parameter
                sharedSavedState = s;
                return; //!
            }
        }
        initOutputBufferAndWriter();
        sharedSavedState = null;
        this.append = append;
        dst = newDst;
    }

    void nestOutputFileEnd(boolean alreadyClosed) throws IOException {
        if (stateStack.size() == 0) {
            throw new RuntimeException(
                    "There is no more saved state in the state-stack!");
        }

        if (sharedSavedState != null) {
            SavedState s = sharedSavedState.sharedSavedState;
            sharedSavedState.store();
            sharedSavedState.sharedSavedState = s;
        } else {
            if (!alreadyClosed) {
                flush();
                fileWriter.close();
               // zipWriter.close();
            }
        }
        SavedState s = (SavedState) stateStack.remove(stateStack.size() - 1);
        s.load();
    }

    void setOutputEncoding(String enc) throws IOException {
        if (fileWriter != null && !(fileWriter instanceof NullWriter)) {
            throw new IOException("Can't change the output encoding becasue "
                    + "some of the output was already written to the file.");
        } else {
            if (enc.equals(Engine.PARAMETER_VALUE_HOST)) {
                this.enc = System.getProperty("file.encoding");
            } else {
                this.enc = enc;
            }
        }
    }

    String getOutputEncoding() {
        return enc;
    }
    
    void setIgnoreFlush(boolean ignore) {
        ignoreFlush = ignore;
    }


    private void createFileWriter() throws IOException {
        File p = dst.getParentFile();
        if (p != null) {
            p.mkdirs();
        }
        // Create a new output stream to write to a file
        if(zipFileWriter== null || zipFileWriter== NullZipOutputStream.INSTANCE) {
            zipFileWriter = new ZipOutputStream(baosClone);
        }
        /*fileWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dst.getPath(), append), enc));*/
      //  baos = new ByteArrayOutputStream();
       // zipWriter=new ZipOutputStream(baos);
        //  zipWriter.putNextEntry(new ZipEntry(dst.getPath()));
        // zipWriter = new ZipOutputStream(new FileOutputStream(dst.getPath(), append));
        //  zipWriter = new ZipOutputStream(new FileOutputStream(dst.getPath(), append));
     //   zipWriter.setMethod(ZipOutputStream.DEFLATED); // specify compression method
      //  zipWriter.closeEntry();
        zipFileWriter.write(baos.toByteArray());
        zipFileWriter.closeEntry();
      //  zipWriter.putNextEntry(new ZipEntry(dst.getPath()));

        baos=new ByteArrayOutputStream();
        fileWriter = new BufferedWriter(new OutputStreamWriter(baos, enc));
        zipFileWriter.putNextEntry(new ZipEntry(dst.getPath()));
    }


    private File deduceNewDst(String newName) throws IOException {
        newName = FileUtil.pathToUnixStyle(newName);
        return FileUtil.resolveRelativeUnixPath(
                engine.getOutputRoot(),
                dst.getParentFile(),
                newName);
    }
    
    public void initOutputBufferAndWriter() {
        fileWriter = null;
      //  baos=new ByteArrayOutputStream();
      //  zipWriter=null;
        if (buf == null) {
            buf = new StringBuffer(BUFFER_SIZE);
        } else {
            buf.delete(0, buf.length());
        }
        freeBuf = BUFFER_SIZE;
    }

    public File getDst() {
        return dst;
    }

    public void setDst(File dst) {
        this.dst = dst;
    }

    public ByteArrayOutputStream getBaosClone() {
        return baosClone;
    }

    public ZipOutputStream getZipFileWriter() {
        return zipFileWriter;
    }

    public ByteArrayOutputStream getBaos() {
        return baos;
    }

/*    private class MyStreamingOutput implements StreamingOutput {
        private final OpenApi2JaxRs generator;

        public MyStreamingOutput(OpenApi2JaxRs generator) {
            this.generator = generator;
        }

        @Override
        public void write(OutputStream output) throws IOException, WebApplicationException {
            generator.generate(output);
        }
    }

    private Response getCodegenProjectAsZip(JaxRsProjectSettings settings, final OpenApi2JaxRs generator) {
        StreamingOutput stream = new MyStreamingOutput(generator);
        String fname = settings.artifactId + ".zip";
        ResponseBuilder builder = Response.ok().entity(stream)
                .header("Content-Disposition", "attachment; filename=\"" + fname + "\"")
                .header("Content-Type", "application/zip");

        return builder.build();
    }*/


    private class SavedState {
        private File dst;
        private String enc;
        private int freeBuf;
        private StringBuffer buf;
        private Writer fileWriter;
      //  private ZipOutputStream zipWriter;
        private boolean append;
        private SavedState sharedSavedState;
        
        private void store() {
            this.dst = FmppFileOutputWriter.this.dst;
            enc = FmppFileOutputWriter.this.enc;
            freeBuf = FmppFileOutputWriter.this.freeBuf;
            buf = FmppFileOutputWriter.this.buf;
            fileWriter = FmppFileOutputWriter.this.fileWriter;
          //  zipWriter = FmppFileOutputWriter.this.zipWriter;
            append = FmppFileOutputWriter.this.append;
            sharedSavedState = FmppFileOutputWriter.this.sharedSavedState;
        }
        
        private void load() {
            FmppFileOutputWriter.this.dst = dst;
            FmppFileOutputWriter.this.enc = enc;
            FmppFileOutputWriter.this.freeBuf = freeBuf;
            FmppFileOutputWriter.this.buf = buf;
            FmppFileOutputWriter.this.append = append;
            FmppFileOutputWriter.this.fileWriter = fileWriter;
          //  zipWriter = FmppFileOutputWriter.this.zipWriter;
            FmppFileOutputWriter.this.sharedSavedState = sharedSavedState;
        }
    }
    
    public File getOutputFile() {
        return dst;
    }
}
