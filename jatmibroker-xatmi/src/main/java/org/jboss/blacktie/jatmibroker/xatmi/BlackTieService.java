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
package org.jboss.blacktie.jatmibroker.xatmi;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.blacktie.jatmibroker.core.transport.JtsTransactionImple;
import org.jboss.blacktie.jatmibroker.core.transport.Message;
import org.jboss.blacktie.jatmibroker.jab.JABException;
import org.jboss.blacktie.jatmibroker.jab.JABTransaction;

/**
 * MDB services implementations extend this class as it provides the core
 * service template method. For non MDB services on the Service interface need
 * be implemented.
 */
public abstract class BlackTieService implements Service {
	/**
	 * The logger to use.
	 */
	private static final Logger log = LogManager
			.getLogger(BlackTieService.class);

	/**
	 * The name of the service.
	 */
	private String name;

	/**
	 * The service needs the name of the service so that it can be resolved in
	 * the btconfig.xml file
	 * 
	 * @param name
	 *            The name of the service
	 */
	public BlackTieService(String name) {
		this.name = name;
		log.debug("Service created: " + name);
	}

	/**
	 * Entry points should pass control to this method as soon as reasonably
	 * possible.
	 * 
	 * @param message
	 *            The message to process
	 * @throws ConfigurationException
	 *             If the connection factory cannot be created
	 * @throws ConnectionException
	 *             In case communication fails
	 */
	protected void processMessage(Message message) throws ConnectionException,
			ConfigurationException {
		Connection connection = ConnectionFactory.getConnectionFactory()
				.getConnection();
		if (message.control != null) {
			try {
				JABTransaction.associateTx(message.control); // associate tx
				// with current
				// thread
			} catch (JABException e) {
				log.warn("Got an invalid tx from queue: " + e);
			}
		}
		if (JtsTransactionImple.hasTransaction()) {
			log
					.error("Blacktie MDBs must not be called with a transactional context");
		} else {
			log.trace("Service invoked");
		}
		log.trace("obtained transport");
		boolean hasTPNOREPLY = (message.flags & Connection.TPNOREPLY) == Connection.TPNOREPLY;

		Session serviceSession = null;
		try {
			serviceSession = connection.createServiceSession(name, message.cd,
					message.replyTo);
			log.debug("Created the session");
			boolean hasTPCONV = (message.flags & Connection.TPCONV) == Connection.TPCONV;
			if (hasTPCONV) {
				int olen = 4;
				X_OCTET odata = new X_OCTET();
				odata.setByteArray("ACK".getBytes());
				long result = serviceSession.tpsend(odata, olen, 0);
				if (result == -1) {
					log.debug("Could not send ack");
					serviceSession.close();
					return;
				} else {
					log.debug("Sent ack");
					serviceSession.setCreatedState(message.flags);
				}
			} else {
				log.debug("cd not being set");
			}

			// THIS IS THE FIRST CALL
			Buffer buffer = null;
			if (message.type != null && !message.type.equals("")) {
				buffer = connection.tpalloc(message.type, message.subtype);
				buffer.deserialize(message.data);
			}
			TPSVCINFO tpsvcinfo = new TPSVCINFO(message.serviceName, buffer,
					message.flags, (hasTPCONV ? serviceSession : null),
					connection, message.len);
			log.debug("Prepared the data for passing to the service");

			boolean hasTx = (message.control != null && message.control
					.length() != 0);

			log.debug("hasTx=" + hasTx + " ior: " + message.control);

			if (hasTx) // make sure any foreign tx is resumed before calling the
				// service routine
				JtsTransactionImple.resume(message.control);

			log.debug("Invoking the XATMI service");
			Response response = null;
			try {
				response = tpservice(tpsvcinfo);
				log.debug("Service invoked");
				if (!hasTPNOREPLY && response == null) {
					log.error("Error, expected response but none returned");
					response = new Response(Connection.TPFAIL,
							Connection.TPESVCERR, null, 0, 0);
				}
			} catch (Throwable t) {
				log.error("Service error detected", t);
				response = new Response(Connection.TPFAIL,
						Connection.TPESVCERR, null, 0, 0);
			}

			if (hasTx) // and suspend it again
				JtsTransactionImple.suspend();

			if (!hasTPNOREPLY && response != null) {
				log.trace("Sending response");
				short rval = response.getRval();
				int rcode = response.getRcode();
				if (connection.hasOpenSessions()) {
					rcode = Connection.TPESVCERR;
					rval = Connection.TPFAIL;
				}
				if (rcode == Connection.TPESVCERR) {
					if (JABTransaction.current() != null) {
						try {
							JABTransaction.current().rollback_only();
						} catch (JABException e) {
							throw new ConnectionException(Connection.TPESYSTEM,
									"Could not mark transaction for rollback only");
						}
					}
				}
				if (rval != Connection.TPSUCCESS && rval != Connection.TPFAIL) {
					rval = Connection.TPFAIL;
				}
				if (rval == Connection.TPFAIL) {
					if (JABTransaction.current() != null) {
						try {
							JABTransaction.current().rollback_only();
						} catch (JABException e) {
							throw new ConnectionException(Connection.TPESYSTEM,
									"Could not mark transaction for rollback only");
						}
					}
				}

				Buffer toSend = response.getBuffer();
				int len = response.getLen();
				String type = null;
				String subtype = null;
				byte[] data = null;
				if (toSend != null) {
					data = toSend.serialize();
					type = toSend.getType();
					subtype = toSend.getSubtype();
					if (!type.equals("X_OCTET")) {
						len = data.length;
					}
				}
				log.debug("Returning desired message");
				// Even though we can provide the cd we don't as atmibroker-xatmi doesn't because tpreturn doesn't
				serviceSession.getSender().send("", rval, rcode, data, len,
						0, response.getFlags(), 0, type, subtype);
			} else if (serviceSession.getSender() == null && response != null) {
				log.error("No sender avaible but message to be sent");
			} else if (serviceSession.getSender() != null && response == null) {
				log.error("Returning error - marking tx as rollback only if ");
				if (JABTransaction.current() != null) {
					try {
						JABTransaction.current().rollback_only();
					} catch (JABException e) {
						throw new ConnectionException(Connection.TPESYSTEM,
								"Could not mark transaction for rollback only");
					}
				}
				log.debug("Returning failed message");
				serviceSession.getSender().send("", Connection.TPFAIL,
						Connection.TPESVCERR, null, 0, 0, 0, 0, null, null);

				log.error("Returned error");
			} else {
				log.debug("No need to send a response");
			}
		} finally {
			connection.close();
		}
	}
}
