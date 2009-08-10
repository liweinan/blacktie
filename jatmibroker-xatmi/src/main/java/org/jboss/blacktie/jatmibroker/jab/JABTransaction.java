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

import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.blacktie.jatmibroker.core.transport.OrbManagement;
import org.jboss.blacktie.jatmibroker.core.util.ThreadActionData;
import org.jboss.blacktie.jatmibroker.core.util.ThreadUtil;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Terminator;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;
import org.omg.CosTransactions.Unavailable;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

public class JABTransaction {
	private static final Logger log = LogManager
			.getLogger(JABTransaction.class);
	static TransactionFactory transactionFactory;
	private JABSession jabSession;
	private int timeout;
	protected Control control;
	private Terminator terminator;

	private Hashtable _childThreads;
	private boolean active = true;
	private OrbManagement orbManagement;

	public void finalize() throws Throwable {
		// TODO use ThreadActionData.purgeAction(this); not popAction
		ThreadActionData.popAction();
		super.finalize();
	}

	public JABTransaction(JABSession aJABSession, int aTimeout)
			throws JABException, NotFound, CannotProceed,
			org.omg.CosNaming.NamingContextPackage.InvalidName, InvalidName,
			AdapterInactive {
		log.debug("JABTransaction constructor");

		if (current() != null)
			throw new JABException("Nested transactions are not supported");

		jabSession = aJABSession;
		timeout = aTimeout;

		control = null;
		terminator = null;

		Properties properties = jabSession.getJABSessionAttributes()
				.getProperties();
		orbManagement = new OrbManagement(properties, true);
		String toLookup = (String) properties.get("blacktie.trans.factoryid");
		org.omg.CORBA.Object aObject = orbManagement.getNamingContextExt()
				.resolve_str(toLookup);
		transactionFactory = TransactionFactoryHelper.narrow(aObject);

		log.debug(" creating Control");
		control = transactionFactory.create(timeout);
		ThreadActionData.pushAction(this);
		log.debug(" created Control " + control);

		setTerminator(control);
	}

	public JABTransaction(String controlIOR) throws JABException {
		JABSessionAttributes sessionAttrs = new JABSessionAttributes(null);
		JABTransaction curr = current();

		jabSession = new JABSession(sessionAttrs);
		timeout = -1;

		try {
			orbManagement = new OrbManagement(sessionAttrs.getProperties(),
					true);
		} catch (org.omg.CORBA.UserException cue) {
			throw new JABException(cue.getMessage(), cue);
		}

		org.omg.CORBA.Object obj = orbManagement.getOrb().string_to_object(
				controlIOR);

		if (curr != null) {
			log.debug("current() != null comparing IORs");
			String pIOR = curr.getControlIOR();
			org.omg.CORBA.Object pObj = orbManagement.getOrb()
					.string_to_object(pIOR);

			log.debug("pIOR=" + pIOR + " pObj=" + pObj);
			if (pObj != null && pObj._is_equivalent(obj)) {
				log.debug("Different IORs same object");
				ThreadActionData.popAction();
			} else {
				log.info("Different IORs and different object");
				throw new JABException("Nested transactions are not supported");
			}
		}

		control = org.omg.CosTransactions.ControlHelper.narrow(obj);

		ThreadActionData.pushAction(this);

		setTerminator(control);
	}

	public boolean equals(java.lang.Object obj) {
		if (obj instanceof JABTransaction) {
			JABTransaction other = (JABTransaction) obj;

			return control.equals(other.control);
		}

		return false;
	}

	public static void associateTx(String controlIOR) throws JABException {
		try {
			// TODO make sure this works in the AS and standalone
			org.jboss.blacktie.jatmibroker.core.transport.JtsTransactionImple
					.resume(controlIOR);
		} catch (Throwable t) {
			new JABTransaction(controlIOR);
		}
	}

	private void setTerminator(Control c) throws JABException {
		try {
			terminator = control.get_terminator();
			log.debug("Terminator is " + terminator);
		} catch (Unavailable e) {
			throw new JABException("Could not get the terminator", e);
		}
	}

	public String getControlIOR() {
		return orbManagement.getOrb().object_to_string(control);
	}

	public static JABTransaction current() {
		return ThreadActionData.currentAction();
	}

	public Control getControl() {
		log.debug("JABTransaction getControl");
		return control;
	}

	public JABSession getSession() {
		log.debug("JABTransaction getSession");
		return jabSession;
	}

	public void commit() throws JABException {
		log.debug("JABTransaction commit");

		try {
			log.debug("calling commit");
			terminator.commit(true);
			active = false;
			ThreadActionData.popAction();
			log.debug("called commit on terminator");
		} catch (Exception e) {
			// TODO build an JABException hierarchy so we can perform better
			// error reporting
			// presume abort and dissassociate the tx from the the current
			// thread
			active = false;
			ThreadActionData.popAction();

			throw new JABException("Could not commit the transaction: "
					+ e.getMessage(), e);
		}
	}

	public void rollback() throws JABException {
		log.debug("JABTransaction rollback");

		try {
			terminator.rollback();
			active = false;
			ThreadActionData.popAction();
			log.debug("called rollback on terminator");
		} catch (Exception e) {
			// presume abort and dissassociate the tx from the the current
			// thread
			active = false;
			ThreadActionData.popAction();

			throw new JABException("Could not rollback the transaction: "
					+ e.getMessage(), e);
		}
	}

	public void rollback_only() throws JABException {
		log.debug("JABTransaction rollback_only");

		try {
			control.get_coordinator().rollback_only();
			log.debug("tx marked rollback only");
		} catch (Unavailable e) {
			throw new JABException(
					"Tx Manager unavailable for set rollback only", e);
		} catch (Exception e) {
			throw new JABException("Error setting rollback only", e);
		}
	}

	/**
	 * Add the specified thread to the list of threads associated with this
	 * transaction.
	 * 
	 * @return <code>true</code> if successful, <code>false</code> otherwise.
	 */
	public final boolean addChildThread(Thread t) {
		if (t == null)
			return false;

		synchronized (this) {
			// if (actionStatus <= ActionStatus.ABORTING)
			if (active) {
				if (_childThreads == null)
					_childThreads = new Hashtable();

				// TODO _childThreads.put(ThreadUtil.getThreadId(t), t); //
				// makes sure so we don't get duplicates

				return true;
			}
		}

		return false;
	}

	/**
	 * Remove a child thread. The current thread is removed.
	 * 
	 * @return <code>true</code> if successful, <code>false</code> otherwise.
	 */
	public final boolean removeChildThread() // current thread
	{
		return removeChildThread(ThreadUtil.getThreadId());
	}

	/**
	 * Remove the specified thread from the transaction.
	 * 
	 * @return <code>true</code> if successful, <code>false</code> otherwise.
	 */
	public final boolean removeChildThread(String threadId) {
		if (threadId == null)
			return false;

		synchronized (this) {
			if (_childThreads != null) {
				_childThreads.remove(threadId);
				return true;
			}
		}

		return false;
	}

	public final JABTransaction parent() {
		return null;
	}

	/**
	 * Suspend the transaction association from the invoking thread. When this
	 * operation returns, the thread will not be associated with a transaction.
	 * 
	 * @return a handle on the current JABTransaction (if any) so that the
	 *         thread can later resume association if required.
	 */
	public static final JABTransaction suspend() {
		JABTransaction curr = ThreadActionData.currentAction();

		if (curr != null)
			ThreadActionData.purgeActions();

		return curr;
	}

	/**
	 * Resume transaction association on the current thread. If the specified
	 * transaction is null, then this is the same as doing a suspend. If the
	 * current thread is associated with a transaction then that association
	 * will be lost.
	 * 
	 * @param JABTransaction
	 *            act the transaction to associate.
	 * @return <code>true</code> if association is successful,
	 *         <code>false</code> otherwise.
	 */
	public static final boolean resume(JABTransaction act) {
		if (act == null)
			suspend();
		else
			ThreadActionData.restoreActions(act);
		return true;
	}
}
