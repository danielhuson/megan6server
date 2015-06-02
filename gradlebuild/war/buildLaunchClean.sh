echo "Test and launch the application with the current settings:"
gradle jettyRunWar
echo "cleaning up"
rm MeganServer.log
gradle clean
