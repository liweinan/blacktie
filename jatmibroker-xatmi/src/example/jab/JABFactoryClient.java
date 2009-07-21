/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.blacktie.jatmibroker.jab.JABException;
import org.jboss.blacktie.jatmibroker.jab.factory.JABBuffer;
import org.jboss.blacktie.jatmibroker.jab.factory.JABConnection;
import org.jboss.blacktie.jatmibroker.jab.factory.JABConnectionFactory;
import org.jboss.blacktie.jatmibroker.jab.factory.JABResponse;
import org.jboss.blacktie.jatmibroker.jab.factory.JABTransaction;

public class JABFactoryClient {
	private static final Logger log = LogManager
			.getLogger(JABFactoryClient.class);

	public static void main(String[] args) throws Exception {
		log.info("JABClient");
		if (args.length != 1) {
			log.error("java JABFactoryClient message");
			return;
		}
		String message = args[0];
		try {
			JABConnectionFactory jcf = JABConnectionFactory.getInstance();
			JABConnection c = jcf.getConnection("connection");
			JABTransaction t = c.beginTransaction(-1);
			JABBuffer b = new JABBuffer();
			b.setValue(message.getBytes());
			log.info("Calling call with input: " + message);
			JABResponse call = c.call("BAR", b, t);
			log.info("Called call with output: " + call.getValue());
			t.commit();
			jcf.closeConnection("connection");
		} catch (JABException e) {
			log.error("JAB error: " + e.getMessage(), e);
		}
	}
}