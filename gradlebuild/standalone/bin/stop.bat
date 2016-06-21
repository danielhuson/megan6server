@ECHO OFF
SET stopport=8181
SET stopkey=7ce31e4eb617c85efbff9450bf9c2fd2
:loop
IF NOT "%1"=="" (
    IF "%1"=="--help" (
        ECHO Usage:
        ECHO --stop-port  Port to stop MeganServer. Default = 8081
        ECHO --stop-key   Stop key for stopping MeganServer. Default = 7ce31e4eb617c85efbff9450bf9c2fd2
        goto :eof
        SHIFT
    )
    IF "%1"=="--stop-port" (
        SET stopport=%2
        SHIFT
    )
    IF "%1"=="--stop-key" (
        SET stopkey=%2
        SHIFT
    )
    SHIFT
    GOTO :loop
)

ECHO Stopping MeganServer with following options:

ECHO Stop Port = %stopport%
ECHO Stop Key = %stopkey%

start javaw -jar ../lib/start.jar  STOP.PORT=%stopport% STOP.KEY=%stopkey% --stop

echo Shut down MeganServer

