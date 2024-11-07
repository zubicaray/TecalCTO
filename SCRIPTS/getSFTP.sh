#!/bin/bash

# Configuration des paramètres
sftpHost='80.11.56.1'
sftpUsername="sftpuser"
sftpPassword=$SFTP_PWD
remotePath="/uploads"
localPath="/home/zubi/DEV/"

# Créer un fichier temporaire pour le script SFTP
sftpScript=$(mktemp)

# Écrire les commandes SFTP dans le fichier temporaire
cat <<EOF > $sftpScript
lcd $localPath
cd $remotePath
mget *.7z
bye
EOF

# Exécuter le script SFTP
sshpass -p $sftpPassword sftp -oBatchMode=no -b $sftpScript $sftpUsername@$sftpHost <<EOF
$sftpPassword
EOF

# Supprimer le fichier temporaire
rm $sftpScript
