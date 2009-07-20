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
package org.jboss.blacktie.jatmibroker.jab;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.blacktie.jatmibroker.xatmi.Buffer;
import org.jboss.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.blacktie.jatmibroker.xatmi.Response;

/**
 * Create an invoker for a remote service.
 */
public class JABRemoteService implements Message {
	private static final Logger log = LogManager
			.getLogger(JABRemoteService.class);
	private Connection connection;
	private String serviceName;
	private Buffer request;
	private Response response;
	boolean noTimeout;

	public JABRemoteService(String aServiceName, JABSession aJABSession)
			throws JABException {
		log.debug("JABService constructor");

		connection = aJABSession.getConnection();
		serviceName = aServiceName;
	}

	public void call(JABTransaction aJABTransaction) throws JABException {
		log.debug("JABService call");

		try {
			// TODO HANDLE TRANSACTION
			response = connection.tpcall(serviceName, request, request
					.getLength(), noTimeout ? Connection.TPNOTIME : 0);
			log.debug("service_request responsed");
		} catch (Exception e) {
			throw new JABException("Could not send tpcall", e);
		}
	}

	public void clear() {
		log.debug("JABService clear");
		request = null;
		response = null;
	}

	public void setString(String string) throws JABException {
		log.debug("JABService set buffer");
		request = new Buffer("X_OCTET", null);
		try {
			request.setData(string.getBytes());
		} catch (ConnectionException e) {
			throw new JABException("Could not write the data: "
					+ e.getMessage(), e);
		}
	}

	public String getString() throws JABException {
		return (String) response.getBuffer().getData();
	}

	public void setNoTimeout(boolean noTimeout) {
		this.noTimeout = noTimeout;
	}
}
