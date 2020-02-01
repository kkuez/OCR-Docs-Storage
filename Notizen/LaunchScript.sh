#!/bin/sh
export LC_ALL=C
export TESSDATA_PREFIX=$PATH/<PATH_TO_TESSDATA>
cd <PATH_TO_OCR.JAR>
sleep 15
echo <PASSWORD> | sudo -S java -jar OCR-Storage.jar -bot