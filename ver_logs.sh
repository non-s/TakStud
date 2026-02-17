#!/bin/bash

# Script para ver os logs da limpeza do Firebase em tempo real

echo "════════════════════════════════════════════════════════"
echo "  📱 LOGS DO TAKSTUD - Limpeza do Firebase"
echo "════════════════════════════════════════════════════════"
echo ""
echo "Aguardando logs..."
echo ""

# Limpar logs antigos
adb logcat -c

# Mostrar apenas logs do TakStud
adb logcat -s TakStud:D
