@echo off

rem RUN THE TXFOOAPP SERVER
IF ["%1"] EQU ["tx"] (
echo "Running txfooapp"
SHIFT
cd %BLACKTIE_HOME%\examples\xatmi\txfooapp
call generate_server -Dservice.names=BAR -Dserver.includes="request.c ora.c DbService.c" -Dx.inc.dir="%ORACLE_HOME%\OCI\include" -Dx.lib.dir="%ORACLE_HOME%\OCI\lib\MSVC" -Dx.libs="oci" -Dx.define="ORACLE"
IF %ERRORLEVEL% NEQ 0 exit -1

IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=

rem RUN THE C CLIENT
call generate_client -Dclient.includes="client.c request.c ora.c cutil.c" -Dx.inc.dir="%ORACLE_HOME%\OCI\include" -Dx.lib.dir="%ORACLE_HOME%\OCI\lib\MSVC" -Dx.libs="oci" -Dx.define="ORACLE"
client
IF %ERRORLEVEL% NEQ 0 exit -1

rem SHUTDOWN THE SERVER RUNNING THE btadmin TOOL
set BLACKTIE_CONFIGURATION=win32
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=
)