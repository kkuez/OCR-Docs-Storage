plink -t -v raspberrypi -l pi -pw XXXPASSWORDXXX "cd Dokumente/Projekte/OCR-Docs-Storage/;  tmux new -d -s nussBot; tmux send-keys -t nussBot.0 'sudo -S java -jar OCR-Storage.jar -bot -DpreferIPv4Stack' ENTER"
exit