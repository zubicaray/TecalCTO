@echo off
set "SFTP_HOST=80.11.56.1"
set "SFTP_USER=sftpuser"
set "SFTP_PASS=tsz67*&àj_0650017490-hacbfhGD"
set "REMOTE_PATH=/uploads"
set "LOCAL_PATH=C:\Anodisation"

:: Crée un script temporaire pour psftp
echo open %SFTP_HOST% > sftp_commands.txt
echo %SFTP_USER% >> sftp_commands.txt
echo %SFTP_PASS% >> sftp_commands.txt
echo lcd %LOCAL_PATH% >> sftp_commands.txt
echo cd %REMOTE_PATH% >> sftp_commands.txt
echo mget *.7z >> sftp_commands.txt
echo quit >> sftp_commands.txt

:: Lance psftp avec le script de commandes
sftp -batch -b sftp_commands.txt

:: Supprime le fichier de commandes
del sftp_commands.txt

:: Extraire tous les fichiers .7z
for %%f in (%LOCAL_PATH%\*.7z) do (
    7z x "%%f" -o%LOCAL_PATH%
)

