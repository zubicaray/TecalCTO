import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from collections import defaultdict

class ZoneCumul:
    def __init__(self, cumul):
        """
        Initialise une instance de ZoneCumul.

        :param cumul: Nombre de postes dans la zone de cumul.
        """
        self.cumul = cumul
        self.lastTimeAtPostes = [0] * cumul  # Tableau initialisé à 0.

    def getPosteIdx(self, starttime, endtime):
        """
        Obtient l'index d'un poste disponible ou met à jour un poste existant.

        :param starttime: Heure de début.
        :param endtime: Heure de fin (non utilisé dans la logique actuelle).
        :param derive: Valeur à assigner au poste sélectionné.
        :return: Index du poste sélectionné.
        """
        zonePrise = False
        idxPoste = 0

        # Parcourt les postes à l'envers
        for i in range(len(self.lastTimeAtPostes) - 1, -1, -1):
            if self.lastTimeAtPostes[i] == 0 and not zonePrise:
                # Si le poste est disponible (non utilisé)
                self.lastTimeAtPostes[i] = endtime
                zonePrise = True
                return i
            elif self.lastTimeAtPostes[i] <= starttime:
                # Si le poste est occupé mais que son temps est dépassé
                self.lastTimeAtPostes[i] = endtime
                if not zonePrise:
                    zonePrise = True
                    idxPoste = i

        return idxPoste  # Retourne l'index trouvé ou le premier disponible

# Exemple de données
tasks = [
    # start, job, index, duration, machine
    {'start': 31665, 'job': 24, 'index': 7, 'duration': 10, 'machine': 8},
    {'start': 31675, 'job': 24, 'index': 8, 'duration': 5, 'machine': 8},
    {'start': 31680, 'job': 25, 'index': 1, 'duration': 350, 'machine': 15},
    {'start': 31695, 'job': 26, 'index': 3, 'duration': 200, 'machine': 15},
    {'start': 31715, 'job': 27, 'index': 4, 'duration': 80, 'machine': 15},
    {'start': 32723, 'job': 27, 'index': 5, 'duration': 10, 'machine': 15},
]

# Séparer les tâches par machine
tasks_by_machine = defaultdict(list)
for task in tasks:
    tasks_by_machine[task['machine']].append(task)


zonesCumul={}
zonesCumul[15]=ZoneCumul(cumul=3)
zonesCumul[33]=ZoneCumul(cumul=2)
zonesCumul[1]=ZoneCumul(cumul=2)
zonesCumul[35]=ZoneCumul(cumul=2)

# Création de la figure
fig, ax = plt.subplots(figsize=(12, 8))

# Couleurs pour différencier les jobs
colors = plt.cm.tab20
job_colors = {}

# Gérer les tâches de la machine 15
machine_15_tasks = tasks_by_machine[15]


# Traiter chaque machine
for machine, machine_tasks in tasks_by_machine.items():
    if machine in [15,1,35,33]:
        # Répartir les tâches de la machine 15 sur 15.1, 15.2 et 15.3
        for task in machine_tasks:
          
            cumulZone=zonesCumul[machine]
            idx =    cumulZone.getPosteIdx(task['start'],task['start']+ task['duration'])-1
            ax.broken_barh(
                [(task['start'], task['duration'])],
                 (machine - ((idx * 0.2) ), 0.2),
                facecolors=job_colors.setdefault(
                    task['job'], colors(len(job_colors) % 20)
                ),
                edgecolor="black",
                label=f"Job {task['job']}" if task['job'] not in job_colors else None,
            )
            
           
    else:
        # Traiter les tâches des autres machines
        for task in machine_tasks:
            ax.broken_barh(
                [(task['start'], task['duration'])],
                (machine - 0.4, 0.8),
                facecolors=job_colors.setdefault(
                    task['job'], colors(len(job_colors) % 20)
                ),
                edgecolor="black",
                label=f"Job {task['job']}" if task['job'] not in job_colors else None,
            )

# Ajouter les étiquettes
ax.set_xlabel("Temps")
ax.set_ylabel("Machines")
ax.set_title("Diagramme de Gantt")
ax.set_yticks(list(tasks_by_machine.keys()) )
ax.set_yticklabels([str(machine) for machine in tasks_by_machine.keys()] )
ax.legend(loc="upper right")

# Afficher la figure
plt.tight_layout()
plt.show()

