# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running all samples"

# RUN THE FOOAPP SERVER
echo "Example: Running fooapp"
cd $BLACKTIE_HOME/examples/xatmi/fooapp
generate_server -Dservice.names=BAR -Dserver.includes=BarService.c
if [ "$?" != "0" ]; then
	exit -1
fi
export BLACKTIE_CONFIGURATION=linux
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

# RUN THE JAVA CLIENT
cd $BLACKTIE_HOME/examples/jab
echo hello | mvn test
if [ "$?" != "0" ]; then
	exit -1
fi

# RUN THE C CLIENT
cd $BLACKTIE_HOME/examples/xatmi/fooapp
generate_client -Dclient.includes=client.c
./client
if [ "$?" != "0" ]; then
	killall -9 server
	exit -1
fi

# RUN THE ADMIN JMX CLIENT
echo "Example: Running Admin Tests"
cd $BLACKTIE_HOME/examples/admin/jmx
echo '0
0
0
0
1' | mvn test
if [ "$?" != "0" ]; then
	exit -1
fi
# RUN THE ADMIN JMX CLIENT
cd $BLACKTIE_HOME/examples/admin/jmx
echo '0
0
0
0
2' | mvn test
if [ "$?" != "0" ]; then
	exit -1
fi
# PICK UP THE CLOSING SERVER
sleep 3

# RUN THE FOOAPP SERVER AGAIN
cd $BLACKTIE_HOME/examples/xatmi/fooapp
export BLACKTIE_CONFIGURATION=linux
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

# SHUTDOWN THE SERVER RUNNING THE XATMI ADMIN CLIENT
cd $BLACKTIE_HOME/examples/admin/xatmi
generate_client -Dclient.includes=client.c
echo '0
0
0
0
1' | ./client
# SHUTDOWN THE SERVER RUNNING THE XATMI ADMIN CLIENT
cd $BLACKTIE_HOME/examples/admin/xatmi
generate_client -Dclient.includes=client.c
echo '0
0
0
0
2' | ./client
# PICK UP THE CLOSING SERVER
sleep 3

# RUN THE QUEUEING EXAMPLE
echo "Example: Running externally managed queue example"
cd $BLACKTIE_HOME/examples/xatmi/queues

generate_client -Dclient.includes=queues.c
./client put 10
if [ "$?" != "0" ]; then
    echo Unable to queue all messages
    exit -1
fi
export BLACKTIE_SERVER_ID=1
./client get 5
if [ "$?" != "0" ]; then
    echo Unable to retrieve first 5 queued messages
    exit -1
fi
./client get 5
if [ "$?" != "0" ]; then
    echo Unable to retrieve last 5 queued messages
    exit -1
fi
unset BLACKTIE_SERVER_ID

# RUN THE TXSENDER EXAMPLE
echo "Example: Running transactional queue example"
cd $BLACKTIE_HOME/examples/xatmi/queues
generate_client -Dclient.includes=txsender.c -Dclient.output.file=txsender
if [ "$?" != "0" ]; then
	exit -1
fi
generate_client -Dclient.includes=queues.c
if [ "$?" != "0" ]; then
	exit -1
fi
echo '1
' | ./txsender
if [ "$?" != "0" ]; then
    echo Unable to queue all messages
    exit -1
fi
export BLACKTIE_SERVER_ID=1
./client get 2
if [ "$?" != "0" ]; then
    echo Unable to retrieve the queued messages
    exit -1
fi
unset BLACKTIE_SERVER_ID

# RUN THE PROPAGATED TRANSACTION EXAMPLE
echo "Example: Running propagated transaction queue example"
cd $BLACKTIE_HOME/examples/xatmi/queues
generate_client -Dclient.includes=queues.c -Dclient.output.file=client
if [ "$?" != "0" ]; then
	exit -1
fi
generate_server -Dserver.includes=BarService.c  -Dservice.names=BAR
if [ "$?" != "0" ]; then
	exit -1
fi
generate_client -Dclient.includes=client.c -Dclient.output.file=clientSender
if [ "$?" != "0" ]; then
	exit -1
fi

btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi
echo '1
' | ./clientSender
if [ "$?" != "0" ]; then
    echo Unable to invoke queue proxy
    exit -1
fi
export BLACKTIE_SERVER=myserv
export BLACKTIE_SERVER_ID=1
./client get 1
if [ "$?" != "0" ]; then
    echo Unable to retrieve the queued message
    exit -1
fi
unset BLACKTIE_SERVER_ID
unset BLACKTIE_SERVER
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi

# RUN THE SECURE SERVER
echo "Example: Running Security"
cd $BLACKTIE_HOME/examples/xatmi/security
generate_server -Dservice.names=SECURE -Dserver.includes=BarService.c
if [ "$?" != "0" ]; then
	exit -1
fi
export BLACKTIE_CONFIGURATION_DIR=serv
export BLACKTIE_CONFIGURATION=linux
btadmin startup secure
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION
unset BLACKTIE_CONFIGURATION_DIR

# RUN THE "guest" USER CLIENT
generate_client -Dclient.includes=client.c
export BLACKTIE_CONFIGURATION_DIR=guest
./client
# This test is expected to fail so make sure the exit status was not 0
if [ "$?" == "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION_DIR

# RUN THE "dynsub" USER CLIENT
export BLACKTIE_CONFIGURATION_DIR=dynsub
./client
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION_DIR

# SHUTDOWN THE SERVER RUNNING THE btadmin TOOL
export BLACKTIE_CONFIGURATION_DIR=serv
export BLACKTIE_CONFIGURATION=linux
btadmin shutdown secure
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION
unset BLACKTIE_CONFIGURATION_DIR

# RUN THE INTEGRATION 1 EXAMPLE
echo "Example: Running integration 1 XATMI"
cd $BLACKTIE_HOME/examples/integration1/xatmi_service/
generate_server -Dservice.names=CREDIT,DEBIT -Dserver.includes="CreditService.c,DebitService.c"
if [ "$?" != "0" ]; then
        exit -1
fi
btadmin startup
if [ "$?" != "0" ]; then
        exit -1
fi
cd $BLACKTIE_HOME/examples/integration1/client/
generate_client -Dclient.includes=client.c 
./client 
if [ "$?" != "0" ]; then
	exit -1
fi
cd $BLACKTIE_HOME/examples/integration1/xatmi_service/
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi

echo "Build Converted XATMI service"
cd $BLACKTIE_HOME/examples/integration1/ejb
mvn install
if [ "$?" != "0" ]; then
	exit -1
fi
cd $BLACKTIE_HOME/examples/integration1/ejb/ear/
mvn install
if [ "$?" != "0" ]; then
	exit -1
fi
cd $BLACKTIE_HOME/examples/integration1/xatmi_adapter/
mvn install
if [ "$?" != "0" ]; then
	exit -1
fi
cd $BLACKTIE_HOME/examples/integration1/xatmi_adapter/ear/
mvn install
if [ "$?" != "0" ]; then
	exit -1
fi
cd $BLACKTIE_HOME/examples/integration1/client/
generate_client -Dclient.includes=client.c
if [ "$?" != "0" ]; then
	exit -1
fi

echo "Run Converted XATMI service"
cd $BLACKTIE_HOME/examples/integration1/ejb/ear/
mvn jboss:deploy
if [ "$?" != "0" ]; then
	exit -1
fi
cd $BLACKTIE_HOME/examples/integration1/xatmi_adapter/ear/
mvn jboss:deploy
if [ "$?" != "0" ]; then
	exit -1
fi
cd $BLACKTIE_HOME/examples/integration1/client/
sleep 5
./client 
if [ "$?" != "0" ]; then
	exit -1
fi
cd $BLACKTIE_HOME/examples/integration1/xatmi_adapter/ear/
mvn jboss:undeploy
if [ "$?" != "0" ]; then
	exit -1
fi
cd $BLACKTIE_HOME/examples/integration1/ejb/ear/
mvn jboss:undeploy
if [ "$?" != "0" ]; then
	exit -1
fi

# RUN THE MDB EXAMPLE
echo "Example: Running MDB examples"
cd $BLACKTIE_HOME/examples/mdb
mvn package jboss:redeploy -DskipTests
if [ "$?" != "0" ]; then
	exit -1
fi
sleep 10
mvn surefire:test
if [ "$?" != "0" ]; then
	exit -1
fi

echo "Example: Running nbf"
cd $BLACKTIE_HOME/examples/nbf
generate_server -Dservice.names=NBF -Dserver.includes=NBFService.c
if [ "$?" != "0" ]; then
	exit -1
fi

export BLACKTIE_CONFIGURATION=linux
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

# RUN THE C CLIENT
cd $BLACKTIE_HOME/examples/nbf
generate_client -Dclient.includes=client.c
./client
if [ "$?" != "0" ]; then
	killall -9 server
	exit -1
fi

export BLACKTIE_CONFIGURATION=linux
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

echo "Example: Running Mapped Service Names"
cd $BLACKTIE_HOME/examples/xatmi/mappedNames
generate_server -Dservice.names=ONE,TWO -Dserver.output.file=hiprio  -Dserver.includes=BarService.c -Dserver.name=hiprio
if [ "$?" != "0" ]; then
	exit -1
fi
generate_server -Dservice.names=THREE,FOUR -Dserver.output.file=loprio  -Dserver.includes=BarService.c -Dserver.name=loprio
if [ "$?" != "0" ]; then
	exit -1
fi

export BLACKTIE_CONFIGURATION=linux
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

cd $BLACKTIE_HOME/examples/xatmi/mappedNames
generate_client -Dclient.includes=client.c
./client
if [ "$?" != "0" ]; then
	killall -9 hiprio
	killall -9 loprio
	exit -1
fi

export BLACKTIE_CONFIGURATION=linux
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

if [ "$1" ]; then
if [ "$1" = "tx" ]; then
echo "Example: Running txfooapp"
shift

# RUN THE FOOAPP SERVER
cd $BLACKTIE_HOME/examples/xatmi/txfooapp
generate_server -Dservice.names=BAR -Dserver.includes="request.c ora.c DbService.c" -Dx.inc.dir="$ORACLE_INC_DIR" -Dx.lib.dir="$ORACLE_LIB_DIR" -Dx.libs="occi clntsh" -Dx.define="ORACLE"
if [ "$?" != "0" ]; then
        exit -1
fi
export BLACKTIE_CONFIGURATION=linux
# use a different logfile for the server
export LOG4CXXCONFIG=log4cxx.server.properties
btadmin startup
if [ "$?" != "0" ]; then
        exit -1
fi

#the client for this test needs to act as a server
#unset BLACKTIE_CONFIGURATION

# RUN THE C CLIENT
cd $BLACKTIE_HOME/examples/xatmi/txfooapp
generate_client -Dclient.includes="client.c request.c ora.c cutil.c" -Dx.inc.dir="$ORACLE_INC_DIR" -Dx.lib.dir="$ORACLE_LIB_DIR" -Dx.libs="occi clntsh" -Dx.define="ORACLE"
# use the default logfile for the client
unset LOG4CXXCONFIG
./client

# SHUTDOWN THE SERVER RUNNING THE btadmin TOOL
export BLACKTIE_CONFIGURATION=linux
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

fi
fi

# LET THE USER KNOW THE OUTPUT
echo "All samples ran OK"
