package io.apicurio.hub.api.fmpp;

import io.apicurio.hub.api.fmpp.setting.Settings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MuleAppGenerator {


    protected Settings settings;
    /**
     * Configure the settings.
     *
     * @param settings
     */
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * Generates a JaxRs project and streams the generated ZIP to the given
     * output stream.
     *
     * @param output
     * @throws IOException
     */
    public final void generate(OutputStream output) throws IOException {
        StringBuilder log = new StringBuilder();

        try (ZipOutputStream zos = new ZipOutputStream(output)) {
            try {
                settings.execute(zos);
            } catch (Exception e) {
                // If we get an error, put an PROJECT_GENERATION_ERROR file into the ZIP.
                zos.putNextEntry(new ZipEntry("PROJECT_GENERATION_FAILED.txt"));
                zos.write("An unexpected server error was encountered while generating the project.  See\r\n".getBytes());
                zos.write("the details of the error below.\r\n\r\n".getBytes());
                zos.write("Generation Log:\r\n\r\n".getBytes());
                zos.write(log.toString().getBytes("utf8"));
                zos.write("\r\n\r\nServer Stack Trace:\r\n".getBytes());

                PrintWriter writer = new PrintWriter(zos);
                e.printStackTrace(writer);
                writer.flush();
                zos.closeEntry();
            }
        }
    }

    

    /**
     * Generate the JaxRs project.
     *
     * @throws IOException
     */
    public ByteArrayOutputStream generate() throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            this.generate(output);
            return output;
        }
    }

}
