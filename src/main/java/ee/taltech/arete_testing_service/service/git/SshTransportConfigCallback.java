package ee.taltech.arete_testing_service.service.git;

import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshTransportConfigCallback implements TransportConfigCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportConfigCallback.class);

    private final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
        @Override
        protected void configure(OpenSshConfig.Host hc, Session session) {
            session.setConfig("StrictHostKeyChecking", "no");
        }
    };

    @Override
    public void configure(Transport transport) {
        try {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        } catch (Exception e) {
            LOGGER.error("SSH authentication failed with message: {}", e.getMessage());
        }
    }
}
