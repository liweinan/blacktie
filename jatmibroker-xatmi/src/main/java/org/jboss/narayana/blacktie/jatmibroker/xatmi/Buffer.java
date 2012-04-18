/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General public  License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General public  License for more details.
 * You should have received a copy of the GNU Lesser General public  License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.blacktie.jatmibroker.xatmi;

import java.io.Serializable;
import java.util.Map;

/**
 * This class is used to send and receive data to and from clients to services.
 * 
 * @see X_OCTET
 * @see X_C_TYPE
 * @see X_COMMON
 */
public interface Buffer extends Serializable {

	/**
	 * Get the format of the message.
	 * 
	 * @return The format of the message
	 */
	public Map<String, Class> getFormat();

	/**
	 * Deserialize the buffer.
	 * 
	 * @param data
	 *            The data to deserialize.
	 * @throws ConnectionException
	 *             In case the data does not match the format defined.
	 */
	public void deserialize(byte[] data) throws ConnectionException;

	/**
	 * Serialize the buffer.
	 * 
	 * @return The byte array for sending.
	 * @throws ConnectionException
	 *             In case the data cannot be formatted correctly
	 */
	public byte[] serialize() throws ConnectionException;

	/**
	 * Get the type
	 * 
	 * @return The type
	 */
	public String getType();

	/**
	 * Get the subtype
	 * 
	 * @return The subtype
	 */
	public String getSubtype();

	/**
	 * Clear the content of the buffer
	 */
	public void clear();

	public int getLen();
}
