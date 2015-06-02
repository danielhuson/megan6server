echo "remove old data"
rm MeganServer-standalone.zip
rm -r MeganServer-standalone/
echo
echo
echo
echo
echo "Building MEGANServer"
echo
echo
gradle assemble
echo
echo
echo "Building finished"
echo
echo
echo
echo "create archive"
mkdir MeganServer-standalone
cp ../../manual/manual.pdf MeganServer-standalone/
cp -r bin MeganServer-standalone/
cp -r lib MeganServer-standalone/
cp -r log MeganServer-standalone/
cp build/libs/MeganServer.war MeganServer-standalone/lib/
rm MeganServer-standalone/log/*
cp -r properties MeganServer-standalone/
zip -r MeganServer-standalone.zip MeganServer-standalone
echo
echo
echo
echo "cleaning up"
gradle clean
rm -r MeganServer-standalone
echo
echo
echo
echo "DONE"

