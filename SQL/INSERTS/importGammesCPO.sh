sqlcmd -S localhost -U sa -P $BDD_PWD -d ANODISATION -i TRUNCATE_CPO_TABLES.sql -C
sqlcmd -S localhost -U sa -P $BDD_PWD -d ANODISATION -i exportGammes.sql -C