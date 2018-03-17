package com.gildedgames.launcher;

import com.gildedgames.launcher.ui.LauncherFrame;
import com.google.common.base.Supplier;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.swing.SwingHelper;
import lombok.extern.java.Log;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;

@Log
public class Start {

    public static void main(final String[] args) {
        Launcher.setupLogger();

        Start.addLetsEncryptSSL();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().setContextClassLoader(Start.class.getClassLoader());
                    UIManager.getLookAndFeelDefaults().put("ClassLoader", Start.class.getClassLoader());
                    UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());

                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                    Launcher launcher = Launcher.createFromArguments(args);
                    launcher.setMainWindowSupplier(new CustomWindowSupplier(launcher));
                    launcher.showLauncherWindow();
                } catch (Throwable t) {
                    log.log(Level.WARNING, "Load failure", t);
                    SwingHelper.showErrorDialog(null, "Uh oh! The updater couldn't be opened because a " +
                            "problem was encountered.", "Launcher error", t);
                }
            }
        });
    }

    private static void addLetsEncryptSSL() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            Path ksPath = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
            keyStore.load(Files.newInputStream(ksPath), "changeit".toCharArray());

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            InputStream caInput = new BufferedInputStream(Start.class.getResourceAsStream("cert.der"));

            try {
                Certificate crt = cf.generateCertificate(caInput);

                log.info("Added Cert for " + ((X509Certificate) crt).getSubjectDN());

                keyStore.setCertificateEntry("DSTRootCAX3", crt);
            } finally {
                caInput.close();
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tmf.getTrustManagers(), null);

            SSLContext.setDefault(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class CustomWindowSupplier implements Supplier<Window> {

        private final Launcher launcher;

        private CustomWindowSupplier(Launcher launcher) {
            this.launcher = launcher;
        }

        @Override
        public Window get() {
            return new LauncherFrame(this.launcher);
        }
    }
}
