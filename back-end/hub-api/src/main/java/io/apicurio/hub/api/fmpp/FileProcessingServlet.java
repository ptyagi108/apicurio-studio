package io.apicurio.hub.api.fmpp;

import io.apicurio.hub.api.fmpp.setting.SettingException;
import io.apicurio.hub.api.fmpp.setting.Settings;
import io.apicurio.hub.api.fmpp.util.MiscUtil;
import io.apicurio.hub.api.fmpp.util.NullZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class FileProcessingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DIRECTORY_PATH = "/path/to/directory"; // Update with your directory path
    private static final String FMPP_TEMPLATE_DIR = "/path/to/fmpp/templates"; // Update with your FMPP template directory path

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String zipFileName = "output.zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");

        try (
                ZipOutputStream zipWriter = new ZipOutputStream(response.getOutputStream());
        ) {
            processDirectory(DIRECTORY_PATH, FMPP_TEMPLATE_DIR, zipWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processDirectory(String directoryPath, String templateDir, ZipOutputStream zipWriter) throws Exception {
        File dir = new File(directoryPath);
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file.getAbsolutePath(), templateDir, zipWriter);
            } else {
                String relativePath = file.getAbsolutePath().substring(directoryPath.length() + 1);
                processFile(file.getAbsolutePath(), relativePath, templateDir, zipWriter);
            }
        }
    }

    private void processFile(String filePath, String relativePath, String templateDir, ZipOutputStream zipWriter) throws Exception {
        try {
            File cfgFile = null;
            cfgFile = new File("."); // load the cfg. of the the current dir.
            Settings ss = new Settings(new File("."));
            ss.load(cfgFile);
         //   ss.addProgressListener(new ConsoleProgressListener());
            ZipOutputStream zos= NullZipOutputStream.INSTANCE;
            ss.execute(zos);
            System.out.println("Done.");
        } catch (SettingException e) {
            System.err.println(MiscUtil.causeMessages(e));
            System.exit(-2);
        } catch (ProcessingException e) {
            System.err.println(MiscUtil.causeMessages(e));
            System.exit(-3);
        }
           ByteArrayOutputStream baos = new ByteArrayOutputStream();


        ZipEntry zipEntry = new ZipEntry(relativePath);
        zipWriter.putNextEntry(zipEntry);
        zipWriter.write(baos.toByteArray());
        zipWriter.closeEntry();
    }
}
