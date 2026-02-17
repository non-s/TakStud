#!/bin/bash

echo "======================================"
echo "  Instalador TakStud - Moto G04s"
echo "======================================"
echo ""

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Caminho do APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

echo -e "${YELLOW}[1/4] Verificando se o APK existe...${NC}"
if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}❌ APK não encontrado em: $APK_PATH${NC}"
    echo "Compilando o APK..."
    ./gradlew assembleDebug
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Erro ao compilar o APK${NC}"
        exit 1
    fi
fi
echo -e "${GREEN}✅ APK encontrado!${NC}"
echo ""

echo -e "${YELLOW}[2/4] Reiniciando servidor ADB...${NC}"
adb kill-server
sleep 1
adb start-server
sleep 2
echo -e "${GREEN}✅ Servidor ADB iniciado!${NC}"
echo ""

echo -e "${YELLOW}[3/4] Verificando dispositivos conectados...${NC}"
DEVICES=$(adb devices | grep -v "List" | grep "device" | wc -l)

if [ $DEVICES -eq 0 ]; then
    echo -e "${RED}❌ Nenhum dispositivo encontrado!${NC}"
    echo ""
    echo "Por favor, verifique:"
    echo "  1. O celular está conectado via USB?"
    echo "  2. A Depuração USB está ativada?"
    echo "  3. Você autorizou o computador no celular?"
    echo ""
    echo "Dispositivos detectados:"
    adb devices -l
    exit 1
fi

echo -e "${GREEN}✅ Dispositivo conectado!${NC}"
adb devices -l
echo ""

echo -e "${YELLOW}[4/4] Instalando TakStud no dispositivo...${NC}"
adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}✅ TakStud instalado com sucesso!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Você pode abrir o aplicativo no seu celular agora!"
    echo "Procure por 'TakStud' na gaveta de aplicativos."
else
    echo ""
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}❌ Erro ao instalar o aplicativo${NC}"
    echo -e "${RED}========================================${NC}"
    echo ""
    echo "Possíveis soluções:"
    echo "  1. Desinstale qualquer versão anterior do TakStud"
    echo "  2. Verifique se há espaço suficiente no celular"
    echo "  3. Tente: adb uninstall com.example.takstud"
    echo "  4. Depois execute este script novamente"
fi
