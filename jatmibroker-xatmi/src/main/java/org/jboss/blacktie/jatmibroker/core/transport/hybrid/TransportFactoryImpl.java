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
package org.jboss.blacktie.jatmibroker.core.transport.hybrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.blacktie.jatmibroker.core.transport.JMSManagement;
import org.jboss.blacktie.jatmibroker.core.transport.OrbManagement;
import org.jboss.blacktie.jatmibroker.core.transport.Transport;
import org.jboss.blacktie.jatmibroker.core.transport.TransportFactory;
import org.jboss.blacktie.jatmibroker.xatmi.ConnectionException;

public class TransportFactoryImpl extends TransportFactory {

	private static final Logger log = LogManager
			.getLogger(TransportFactoryImpl.class);
	private Properties properties;
	private OrbManagement orbManagement;
	private JMSManagement jmsManagement;
	private List<Transport> transports = new ArrayList<Transport>();

	protected void initialize(Properties properties)
			throws ConfigurationException {
		log.debug("Creating OrbManagement");
		this.properties = properties;

		try {
			jmsManagement = new JMSManagement(properties);
		} catch (Throwable t) {
			throw new ConfigurationException(
					"Could not create the required connection", t);
		}

		try {
			orbManagement = new OrbManagement(properties, false);
		} catch (Throwable t) {
			throw new ConfigurationException(
					"Could not create the orb management function", t);
		}

		log.debug("Created OrbManagement");
	}

	public synchronized Transport createTransport() throws ConnectionException {
		log.debug("Creating transport from factory: " + this);
		TransportImpl instance = null;
		try {
			instance = new TransportImpl(orbManagement, jmsManagement,
					properties, this);
		} catch (Throwable t) {
			throw new ConnectionException(
					org.jboss.blacktie.jatmibroker.xatmi.Connection.TPESYSTEM,
					"Could not connect to server", t);
		}
		transports.add(instance);
		log.debug("Creating transport from factory: " + this + " transport: "
				+ instance);
		return instance;
	}

	public synchronized void closeFactory() {
		log.debug("Closing factory: " + this);
		Transport[] transport = new Transport[transports.size()];
		transport = transports.toArray(transport);
		for (int i = 0; i < transport.length; i++) {
			try {
				log.debug("Closing transport: " + transport[i]
						+ " from factory: " + this);
				transport[i].close();
			} catch (ConnectionException e) {
				log.warn("Transport could not be closed: " + e.getMessage(), e);
			}
		}
		orbManagement.close();
		jmsManagement.close();
	}

	public void removeTransport(TransportImpl transportImpl) {
		boolean remove = transports.remove(transportImpl);
		if (remove) {
			log.debug("Transport was removed: " + transportImpl + " from: "
					+ this);
		} else {
			log.error("Transport was not removed: " + transportImpl + " from: "
					+ this);
		}
	}
}
