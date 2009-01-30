/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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
/*
 * BREAKTHRUIT PROPRIETARY - NOT TO BE DISCLOSED OUTSIDE BREAKTHRUIT, LLC.
 */
// copyright 2006, 2008 BreakThruIT

#include <iostream>
#include <stdio.h>

#include "xatmi.h"
#include "AtmiBrokerClient.h"
#include "AtmiBroker_ClientCallbackImpl.h"
#include "AtmiBrokerClientXml.h"
#include "userlog.h"

#include "log4cxx/logger.h"
using namespace log4cxx;
using namespace log4cxx::helpers;
LoggerPtr loggerAtmiBrokerClient(Logger::getLogger("AtmiBrokerClient"));

AtmiBrokerClient::AtmiBrokerClient() {
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "constructor ");

	AtmiBrokerClientXml aAtmiBrokerClientXml;
	aAtmiBrokerClientXml.parseXmlDescriptor(&clientServerVector, "CLIENT.xml");

	clientCallbackImpl = new AtmiBroker_ClientCallbackImpl(client_poa);
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "tmp_servant %p", (void*) clientCallbackImpl);

	client_poa->activate_object(clientCallbackImpl);
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "activated tmp_servant %p", (void*) clientCallbackImpl);

	CORBA::Object_ptr tmp_ref = client_poa->servant_to_reference(clientCallbackImpl);
	clientCallback = AtmiBroker::ClientCallback::_narrow(tmp_ref);
	clientCallbackIOR = client_orb->object_to_string(clientCallback);
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "created AtmiBroker::ClientCallback %s", (char*) clientCallbackIOR);

	for (std::vector<ClientServerInfo*>::iterator itServer = clientServerVector.begin(); itServer != clientServerVector.end(); itServer++) {
		userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "next serverName is: %s", (char*) (*itServer)->serverName);

		for (std::vector<char*>::iterator itService = (*itServer)->serviceVectorPtr->begin(); itService != (*itServer)->serviceVectorPtr->end(); itService++) {
			userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "next serviceName is: %s", (char*) (*itService));
			serviceNameArray.push_back((char*) (*itService));
		}
	}
}

AtmiBrokerClient::~AtmiBrokerClient() {
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "destructor ");

	for (std::vector<ClientServerInfo*>::iterator itServer = clientServerVector.begin(); itServer != clientServerVector.end(); itServer++) {
		userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "next serverName is: %s", (char*) (*itServer)->serverName);
	}
	clientServerVector.clear();

	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "freeing serviceNames from array ");
	for (unsigned int i = 0; i < serviceNameArray.size(); i++) {
		delete serviceNameArray[i];
	}
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "freed serviceNames from array ");

	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "freeing serviceNameArray ");
	serviceNameArray.clear();
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "freed serviceNameArray ");
}

AtmiBroker_ClientCallbackImpl * AtmiBrokerClient::getClientCallback() {
	return clientCallbackImpl;
}

char* AtmiBrokerClient::getClientCallbackIOR() {
	return clientCallbackIOR;
}

void AtmiBrokerClient::findService(char * serviceAndIndex, AtmiBroker::Service_var * refPtr) {
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "findService(): %s", serviceAndIndex);

	char index[5];
	unsigned int i = 0;
	unsigned int j = 0;

	char *serviceName = (char*) malloc(sizeof(char) * XATMI_SERVICE_NAME_LENGTH);

	for (i = 0; i < strlen(serviceAndIndex) && serviceAndIndex[i] != ':'; i++) {
		serviceName[i] = serviceAndIndex[i];
	}
	serviceName[i] = '\0';
	i++;
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "serviceName is %s", serviceName);

	for (; i < strlen(serviceAndIndex); i++) {
		index[j] = serviceAndIndex[i];
		j++;
	}
	index[j] = '\0';
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "index is %s", index);

	//	find_service(getClientId(serviceName), get_service_factory(serviceName), index, refPtr);
	free(serviceName);
}

void AtmiBrokerClient::extractServiceAndIndex(char * serviceAndIndex, char * serviceName, char * index) {
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "extractServiceAndIndex(): %s", serviceAndIndex);

	unsigned int i = 0;
	unsigned int j = 0;

	for (i = 0; i < strlen(serviceAndIndex) && serviceAndIndex[i] != ':'; i++) {
		serviceName[i] = serviceAndIndex[i];
	}
	serviceName[i] = '\0';
	i++;
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "serviceName is %s", serviceName);

	for (; i < strlen(serviceAndIndex); i++) {
		index[j] = serviceAndIndex[i];
		j++;
	}
	index[j] = '\0';
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "index is %s", index);
}

int AtmiBrokerClient::convertIdToInt(char * id) {
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "convertIdToInt %s", id);

	int anIntId = 0;
	const char *svcName;

	for (unsigned int i = 0; i < serviceNameArray.size(); i++) {
		char *serviceAndColon = (char*) malloc(sizeof(char*) * XATMI_SERVICE_NAME_LENGTH);
		memset(serviceAndColon, '\0', XATMI_SERVICE_NAME_LENGTH);
		strcpy(serviceAndColon, serviceNameArray[i]);
		strcat(serviceAndColon, ":");
		if (strncmp(id, serviceAndColon, strlen(serviceAndColon)) == 0) {
			userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "found matching service %s", serviceNameArray[i]);
			free(serviceAndColon);
			svcName = serviceNameArray[i];
			anIntId = (i + 1) * 1000;
			break;
		} else
			free(serviceAndColon);
	}
	int position = -1;
	for (unsigned int i = 0; i < strlen(id); i++) {
		if (id[i] == ':') {
			position = i;
		}
	}

	char serviceName[XATMI_SERVICE_NAME_LENGTH + 1];
	strncpy(serviceName, id, position);
	svcName = serviceName;
	serviceName[position] = '\0';

	for (unsigned int j = strlen(svcName) + 1; j < strlen(id); j++) {
		int c = id[j];
		anIntId += (c - 48);
	}

	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "anIntId %d", anIntId);
	return anIntId;
}

char*
AtmiBrokerClient::convertIdToString(int id) {
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "convertIdToString %d", id);

	char * toReturn = NULL;
	char *anIdStr = (char*) malloc(sizeof(char*) * XATMI_SERVICE_NAME_LENGTH);

	unsigned int i = id / 1000;
	int index = id - (i * 1000);

	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "i is %d", i);
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "index is %d", index);

	// TODO ADD CHECK THAT THE ARRAY POSITION EXISTS
	if (i > 0 && i <= serviceNameArray.size()) {
		strcpy(anIdStr, serviceNameArray[i - 1]);
		strcat(anIdStr, ":");
		// ltoa
		std::ostringstream oss;
		oss << index << std::dec;
		const char* indexStr = oss.str().c_str();

		strcat(anIdStr, indexStr);

		userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "anIdStr %s", anIdStr);
		toReturn = strdup(anIdStr);
	}
	free(anIdStr);
	return toReturn;
}

AtmiBroker::ServiceFactory_ptr AtmiBrokerClient::get_service_factory(const char * serviceName) {
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "get_service_factory: %s", serviceName);

	CosNaming::Name * name = client_default_context->to_name(serviceName);
	userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "Service name %p", (void*) name);

	AtmiBroker::ServiceFactory_ptr factoryPtr = NULL;

	try {
		CORBA::Object_var tmp_ref = client_name_context->resolve(*name);
		userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "Service ref %p", (void*) tmp_ref);
		factoryPtr = AtmiBroker::ServiceFactory::_narrow(tmp_ref);
	} catch (...) {
		userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "Could not access the service factory %p", (void*) name);
	}

	if (CORBA::is_nil(factoryPtr)) {
		userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "Could not retrieve Factory for %s", serviceName);
	} else
		userlog(Level::getDebug(), loggerAtmiBrokerClient, (char*) "retrieved  %s Factory %p", serviceName, (void*) factoryPtr);

	return AtmiBroker::ServiceFactory::_duplicate(factoryPtr);
}
