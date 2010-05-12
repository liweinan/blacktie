package org.jboss.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.blacktie.jatmibroker.xatmi.Response;
import org.jboss.blacktie.jatmibroker.xatmi.Service;
import org.jboss.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.blacktie.jatmibroker.xatmi.TestSpecExampleOne;
import org.jboss.blacktie.jatmibroker.xatmi.X_C_TYPE;

public class SpecExampleOneService implements Service {
	private static final Logger log = LogManager
			.getLogger(SpecExampleOneService.class);

	public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException {
		log.info("debit_credit_svc");
		short rval;
		/* extract request typed buffer */
		X_C_TYPE dc_ptr = (X_C_TYPE) svcinfo.getBuffer();
		/*
		 * Depending on service name used to invoke this routine, perform either
		 * debit or credit work.
		 */
		if (!svcinfo.getName().equals("DEBIT")) {
			/*
			 * Parse input data and perform debit as part of global transaction.
			 */
		} else {
			/*
			 * Parse input data and perform credit as part of global
			 * transaction.
			 */
		}
		// TODO MAKE TWO TESTS
		if (dc_ptr.getInt("failTest") == 0) {
			rval = Connection.TPSUCCESS;
			dc_ptr.setInt("output", TestSpecExampleOne.OK);
		} else {
			rval = Connection.TPFAIL; /* global transaction will not commit */
			dc_ptr.setInt("output", TestSpecExampleOne.NOT_OK);
		}
		/* send reply and return from service routine */
		return new Response(rval, 0, dc_ptr, 0, 0);
	}
}
