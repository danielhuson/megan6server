echo "We build, create an archive for the website, and clean up"
echo
echo
echo
echo "assemble"
gradle assemble
echo
echo
echo
echo "copy"
mkdir MeganServer-webarchive
cp build/libs/MeganServer.war MeganServer-webarchive/
cp ../../tex/manual/manual.pdf MeganServer-webarchive/
echo
echo
echo
echo "create archive"
zip -r MeganServer-webarchive.zip MeganServer-webarchive
echo
echo
echo
echo "clean up"
rm -r MeganServer-webarchive/
gradle clean


