echo "Killing NussBot Session"
sh ./killNussBot.sh
echo "=================================================="
echo "Copying new Jar"
sh ./copyNewJar.sh
echo "=================================================="
echo "Starting new NussBot Session"
sh ./restartNussBot.sh
echo "=================================================="
echo "Done."
sleep 10