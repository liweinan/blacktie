# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Example: Running Security example"

# RUN THE SECURE SERVER
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
