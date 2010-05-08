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
package org.jboss.blacktie.jatmibroker.core.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.blacktie.jatmibroker.core.transport.Receiver;
import org.jboss.blacktie.jatmibroker.core.transport.Transport;
import org.jboss.blacktie.jatmibroker.core.transport.TransportFactory;
import org.jboss.blacktie.jatmibroker.xatmi.Service;
import org.jboss.blacktie.jatmibroker.xatmi.ConnectionException;

public class ServiceData {

	private static final Logger log = LogManager.getLogger(ServiceData.class);
	private static final String DEFAULT_POOL_SIZE = "1";
	private Receiver receiver;
	private List<ServiceDispatcher> dispatchers = new ArrayList<ServiceDispatcher>();
	private Transport connection;
	private String serviceName;

	public ServiceData(Properties properties, String serviceName,
			String serviceClassName) throws ConnectionException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, ConfigurationException {
		this.serviceName = serviceName;

		String sizeS = properties.getProperty("blacktie." + serviceName
				+ ".size", DEFAULT_POOL_SIZE);
		int size = Integer.parseInt(sizeS);

		connection = TransportFactory.loadTransportFactory(serviceName,
				properties).createTransport();
		this.receiver = connection.getReceiver(serviceName);

		Class callback = Class.forName(serviceClassName);
		for (int i = 0; i < size; i++) {
			dispatchers.add(new ServiceDispatcher(serviceName,
					(Service) callback.newInstance(), receiver));
		}
	}

	public void close() throws ConnectionException {
		log.debug("Unadvertising: " + serviceName);

		// Clean up the consumers
		Iterator<ServiceDispatcher> iterator = dispatchers.iterator();
		while (iterator.hasNext()) {
			iterator.next().startClose();
		}

		// Disconnect the receiver
		receiver.close();
		// Disconnect the transport
		connection.close();

		// Clean up the consumers
		iterator = dispatchers.iterator();
		while (iterator.hasNext()) {
			iterator.next().close();
		}
		dispatchers.clear();
		log.info("Unadvertised: " + serviceName);
	}

	public String getServiceName() {
		return serviceName;
	}
}
