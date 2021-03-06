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
/* Portable Buffer Functions */
#include "TestPBF.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestPBF);
/* End of Portable Buffer Functions */
/* Typed Buffer Functions */
#include "TestTPAlloc.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPAlloc);
#include "TestTPTypes.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPTypes);
#include "TestTPFree.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPFree);
#include "TestTPFreeService.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPFreeService);
#include "TestTPRealloc.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPRealloc);
/* End of Typed Buffer Functions */
/* Dynamic Service Management */
#include "TestTPUnadvertise.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPUnadvertise);
#include "TestTPAdvertise.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPAdvertise);
/* End of Dynamic Service Management */
/* Request Response */
#include "TestTPCall.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPCall);
#include "TestTPACall.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPACall);
#include "TestTPCancel.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPCancel);
#include "TestTPGetRply.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPGetRply);
/* End of Request Response */
/* Service Routing*/
#include "TestTPService.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPService);
#include "TestTPReturn.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPReturn);
/* End of Service Routing*/
/* Conversation */
#include "TestTPConnect.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPConnect);
#include "TestTPDiscon.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPDiscon);
#include "TestTPRecv.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPRecv);
#include "TestTPSend.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPSend);
#include "TestTPConversation.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPConversation);
/* End of Conversation */
/* Quickstarts from the specification */
#include "TestSpecQuickstartOne.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestSpecQuickstartOne);
#include "TestSpecQuickstartTwo.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestSpecQuickstartTwo);
/* End of Quickstarts from the specification */
/* Application Management Functions */
#include "LoopyServerAndClient.h"
CPPUNIT_TEST_SUITE_REGISTRATION( LoopyServerAndClient);
#include "TestServerinit.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestServerinit);
#include "TestClientInit.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestClientInit);
/* End of Application Management Functions */
/* Test of Service with topic */
#include "TestTopic.h"
CPPUNIT_TEST_SUITE_REGISTRATION( TestTopic);
/* End of Test Topic*/
