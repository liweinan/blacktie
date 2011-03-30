# ALLOW JOBS TO BE BACKGROUNDED
set -m

EXAMPLE_HOME=$BLACKTIE_HOME/examples/xatmi/replyTo

echo "Example: Running example to show reply to"
# GENERATE AN EMPTY SERVER
cd $EXAMPLE_HOME
generate_server -Dserver.includes=BarService.c
if [ "$?" != "0" ]; then
	exit -1
fi

# RUN THE SERVER - MUST PROVIDE BLACKTIE_CONFIGURATION
# AS THIS EXAMPLE USES IT TO FIND THE CORRECT LIBRARY_NAME
cd $EXAMPLE_HOME
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi

# BUILD THE C CLIENT
cd $EXAMPLE_HOME
generate_client -Dclient.includes=client.c

# RUN THE C clientA
cd $EXAMPLE_HOME
echo '0
0' | ./client clientA CLIENTRESPONSEHANDLER_1 hello1
if [ "$?" != "0" ]; then
	killall -9 server
	exit -1
fi

# RUN THE C clientB
cd $EXAMPLE_HOME
echo '0
0' | ./client clientB CLIENTRESPONSEHANDLER_2 hello2
if [ "$?" != "0" ]; then
	killall -9 server
	exit -1
fi


# SHUTDOWN THE SERVER
BLACKTIE_SERVER=server btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi