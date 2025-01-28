import pyodbc

# Connexion à votre base de données SQL Server
conn = pyodbc.connect(
    "Driver={ODBC Driver 17 for SQL Server};"
    "Server=localhost;"
    "Database=ANODISATION;"
    "UID=sa;"  # Utilisateur sa
    "PWD=Jeff_nenette;" 
)

# Exécution de la requête SQL
query = """
select 
    DF.numficheproduction,DG.NumZone,DG.TempsAuPosteSecondes ,Z.derive
from   
    [DetailsGammesProduction]  DG 
    RIGHT OUTER JOIN   [DetailsFichesProduction] DF 
    on   	DG.numficheproduction=DF.numficheproduction  
    COLLATE FRENCH_CI_AS  and 	DG.numligne=DF.NumLigne and DG.NumPosteReel=DF.NumPoste  
    INNER JOIN ZONES  Z on DG.Numzone=Z.Numzone  
  
    where DF.numficheproduction in 
    ('00085171','00085172','00085173')
    --,'00085174','00085175','00085176','00085177',    '00085178','00085179','00085180',
    --'00085181','00085182','00085183','00085184',    '00085185','00085186','00085187','00085188','00085189','00085190','00085191',    '00085192','00085193','00085194','00085195','00085196','00085197','00085198',    '00085199','00085200','00085201','00085202','00085203','00085204','00085205',    '00085206','00085207','00085208','00085209','00085210','00085211','00085212',    '00085213','00085214','00085215','00085216')    
    order by DF.numficheproduction, DG.NumLigne
"""
cursor = conn.cursor()
cursor.execute(query)

# Récupérer les résultats
results = cursor.fetchall()

# Organiser les résultats en Python
jobs_data = {}

for row in results:
    numficheproduction = row.numficheproduction
    num_zone = row.NumZone
    if num_zone in [1,35]:
        temps=100
    else:
        temps = row.TempsAuPosteSecondes 
    derive = row.derive
    
    # Ajout des données par job
    if numficheproduction not in jobs_data:
        jobs_data[numficheproduction] = []
    jobs_data[numficheproduction].append((num_zone, temps,derive))

# Convertir le dictionnaire en liste imbriquée
jobs_data_list = [jobs_data[job] for job in sorted(jobs_data)]

# Afficher le résultat
print(jobs_data_list)

# Fermer la connexion
cursor.close()
conn.close()
