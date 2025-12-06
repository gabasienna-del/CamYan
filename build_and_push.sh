#!/bin/bash

echo "===== СБОРКА APK ====="
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "===== СБОРКА УСПЕШНА! ДЕЛАЮ PUSH ====="
    git add .
    git commit -m "auto-build $(date '+%Y-%m-%d %H:%M')"
    git push origin main
    echo "===== PUSH ЗАВЕРШЁН ====="
else
    echo "===== ОШИБКА СБОРКИ — PUSH ОТМЕНЁН ====="
fi
