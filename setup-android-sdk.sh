#!/bin/bash
set -e

# 🚀 Instalação do Android SDK via Linha de Comando + Geração de APK com Gradle

echo "📦 Baixando o SDK..."
wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip
echo "Concluindo, aguarde..."
sleep 2

echo "📁 Criando estrutura de pastas e descompactando..."
mkdir -p ~/Android/Sdk/cmdline-tools/
unzip commandlinetools-linux-*.zip -d ~/Android/Sdk/cmdline-tools/
echo "Aguarde..."
sleep 2

echo "🏷️ Renomeando a pasta para 'latest'..."
mv ~/Android/Sdk/cmdline-tools/cmdline-tools/ ~/Android/Sdk/cmdline-tools/latest
sleep 1

echo "�� Removendo o arquivo ZIP..."
rm commandlinetools-linux-*.zip
sleep 1

echo "⚙️ Configurando variáveis de ambiente e local.properties..."
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
sleep 1

echo "✅ Dando permissão de execução ao Gradle wrapper..."
chmod +x gradlew
sleep 1

echo "📜 Aceitando os termos de uso do SDK..."
yes | sdkmanager --licenses
echo "Só mais um pouco..."
sleep 2

echo "🛠️ Gerando APK com Gradle..."
./gradlew assembleDebug

echo "✅ APK gerado em: app/build/outputs/apk/debug/app-debug.apk"
