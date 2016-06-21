@ECHO OFF
SET port=8080
SET stopport=8181
SET stopkey=7ce31e4eb617c85efbff9450bf9c2fd2
SET xmx=4G
SET name=MeganServer
SET tab=
:loop
IF NOT "%1"=="" (
    IF "%1"=="--help" (
        ECHO Usage:
        ECHO --name       Define the name of MeganServer instance. Default = MeganServer
        ECHO --port       Port on which MeganServer will be launched. Default = 8080
        ECHO --stop-port  Port to stop MeganServer. Default = 8081
        ECHO --stop-key   Stop key for stopping MeganServer. Default = 7ce31e4eb617c85efbff9450bf9c2fd2
        ECHO --max-memory Max memory for MeganServer. Default = 4G
        goto :eof
        SHIFT
    )
    IF "%1"=="--port" (
        SET port=%2
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
    IF "%1"=="--name" (
        SET name=%2
        SHIFT
    )
    IF "%1"=="--max-memory" (
        SET xmx=%2
        SHIFT
    )
    SHIFT
    GOTO :loop
)
ECHO Launching MeganServer with following options:
ECHO Port = %port%
ECHO Stop Port = %stopport%
ECHO Stop Key = %stopkey%
ECHO Max Memory = %xmx%
ECHO Name = %name%

echo Starting MeganServer...

start javaw -Xmx%xmx% -jar ../lib/jetty-runner-9.2.7.v20150116.jar --port %port% --out ../log/yyyy_mm_dd.log --classes ../properties/ --path /%name% ../lib/MeganServer.war --stop-port %stopport% --stop-key %stopkey%

echo MeganServer started. Test connection under http://www.localhost:%port%/%name%. Check log files for the case that no connection can be established.

