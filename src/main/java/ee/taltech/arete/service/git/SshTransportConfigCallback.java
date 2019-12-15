package ee.taltech.arete.service.git;

import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;

public class SshTransportConfigCallback implements TransportConfigCallback {

	private final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
		@Override
		protected void configure(OpenSshConfig.Host hc, Session session) {
			session.setConfig("StrictHostKeyChecking", "no");
		}
	};

	@Override
	public void configure(Transport transport) {
		SshTransport sshTransport = (SshTransport) transport;
		sshTransport.setSshSessionFactory(sshSessionFactory);
	}

}
