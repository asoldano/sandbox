import ezodf
import pandas as pd
import requests
from datetime import datetime, timezone
import argparse

def read_ods(file_path):
    """
    Legge un file .ods e restituisce una lista di URL dalla prima colonna.
    """
    # Carica il file ODS
    doc = ezodf.opendoc(file_path)
    sheet = doc.sheets[0]  # Assumiamo che il primo foglio contenga i dati

    # Estrai i dati dalla prima colonna
    urls = []
    for row in sheet.rows():
        cell = row[0]  # Prima colonna
        if cell.value is not None:  # Salta celle vuote
            urls.append(cell.value)
    
    return urls

from datetime import datetime, timezone

def analyze_repo(repo_url, github_token, start_date=None):
    """
    Analizza le Pull Requests di un repository su GitHub con supporto per la paginazione e filtro sulla data.
    """
    try:
        # Estrai owner e repo dall'URL
        _, owner, repo = repo_url.rstrip('/').rsplit('/', 2)
        repo = repo.replace('.git', '')  # Rimuovi ".git" dal nome del repository se presente


        # GitHub API endpoint
        base_url = f"https://api.github.com/repos/{owner}/{repo}/pulls"
        headers = {"Authorization": f"token {github_token}"}
        params = {"state": "all", "per_page": 100}  # Recupera fino a 100 PR per pagina
        pulls = []
        page = 1

        # Se fornita, converti la data di inizio in un oggetto datetime
        start_datetime = None
        if start_date:
            start_datetime = datetime.strptime(start_date, "%Y-%m-%d").replace(tzinfo=timezone.utc)
            print(f"Filtrando PR create dopo {start_datetime}")

        # Paginazione per ottenere tutte le PR
        while True:
#            print(f"Richiesta pagina {page} delle Pull Requests...")
            params["page"] = page
            response = requests.get(base_url, headers=headers, params=params)

            if response.status_code == 404:
                print(f"Errore 404: il repository '{repo}' non è stato trovato o il token non ha accesso.")
                return {
                    "repo": repo,
                    "num_prs": 0,
                    "avg_close_time_hours": None,
                    "num_open_prs": 0,
                    "avg_age_open_prs_hours": None,
                }
            if response.status_code != 200:
                print(f"Errore per {repo}: {response.json()}")
                return {
                    "repo": repo,
                    "num_prs": 0,
                    "avg_close_time_hours": None,
                    "num_open_prs": 0,
                    "avg_age_open_prs_hours": None,
                }

            data = response.json()
            if not data:  # Nessuna PR nella pagina, fine della paginazione
                break

            # Filtra la pagina corrente in base alla data di inizio
            all_older_than_start_date = True
            for pr in data:
                # Escludi PR create da Dependabot o altri bot
                if pr['user']['type'] == 'Bot' or pr['user']['login'].startswith("dependabot"):
                    continue

                created_at = datetime.strptime(pr['created_at'], "%Y-%m-%dT%H:%M:%SZ").replace(tzinfo=timezone.utc)
                if start_datetime and created_at >= start_datetime:
                    pulls.append(pr)
                    all_older_than_start_date = False  # Trovata almeno una PR valida
                elif start_datetime and created_at < start_datetime:
                    continue

            if all_older_than_start_date:
#                print("Tutte le PR della pagina sono precedenti alla data di inizio. Interruzione.")
                break  # Interrompi il ciclo di paginazione se tutte le PR sono troppo vecchie

            page += 1

        print(f"Totale Pull Requests trovate per {repo}: {len(pulls)}")

        # Analizza le PR
        times_to_close = []
        open_pr_ages = []
        num_open_prs = 0
        now = datetime.now(timezone.utc)  # Usa timezone-aware datetime

        for pr in pulls:
            created_at = datetime.strptime(pr['created_at'], "%Y-%m-%dT%H:%M:%SZ").replace(tzinfo=timezone.utc)
            closed_at = pr.get('closed_at')

            if closed_at:
                closed_at_dt = datetime.strptime(closed_at, "%Y-%m-%dT%H:%M:%SZ").replace(tzinfo=timezone.utc)
                times_to_close.append((closed_at_dt - created_at).total_seconds())
            else:
                # Calcola l'età delle PR aperte
                open_pr_ages.append((now - created_at).total_seconds())
                num_open_prs += 1

        # Calcola statistiche
        avg_close_time = (sum(times_to_close) / len(times_to_close)) / 3600 if times_to_close else None
        avg_age_open_prs = (sum(open_pr_ages) / len(open_pr_ages)) / 3600 if open_pr_ages else None

        return {
            "repo": repo,
            "num_prs": len(pulls),
            "avg_close_time_hours": avg_close_time,
            "num_open_prs": num_open_prs,
            "avg_age_open_prs_hours": avg_age_open_prs,
        }
    except Exception as e:
        print(f"Errore nell'analisi del repository {repo_url}: {e}")
        return {
            "repo": repo,
            "num_prs": 0,
            "avg_close_time_hours": None,
            "num_open_prs": 0,
            "avg_age_open_prs_hours": None,
        }

def main(ods_path, github_token, output_file, start_date=None):
    """
    Legge gli URL dal file .ods, analizza le PR su GitHub e salva i risultati.
    """
    # Leggi gli URL dal file ODS
    repos = read_ods(ods_path)
    print(f"Trovati {len(repos)} repository nel file ODS.")

    # Analizza tutti i repository
    results = [analyze_repo(url, github_token, start_date) for url in repos]

    # Filtra i risultati non validi e crea il DataFrame
    filtered_results = [
        {
            "repo": r["repo"],
            "num_prs": r["num_prs"],
            "avg_close_time_hours": r["avg_close_time_hours"],
            "num_open_prs": r["num_open_prs"],
            "avg_age_open_prs_hours": r["avg_age_open_prs_hours"],
        }
        for r in results if r is not None
    ]

    # Esporta in CSV
    df = pd.DataFrame(filtered_results)
    df.to_csv(output_file, index=False)
    print(f"Analisi completata! Risultati salvati in {output_file}")


if __name__ == "__main__":
    # Configura gli argomenti da riga di comando
    parser = argparse.ArgumentParser(description="Analisi dei tempi di chiusura delle PR su GitHub da file .ods")
    parser.add_argument("--ods", required=True, help="Percorso al file .ods contenente gli URL dei repository")
    parser.add_argument("--token", required=True, help="Token personale di accesso GitHub")
    parser.add_argument("--output", default="pull_requests_analysis.csv", help="File di output (default: pull_requests_analysis.csv)")
    parser.add_argument("--start-date", help="Data di inizio nel formato YYYY-MM-DD (opzionale)")

    args = parser.parse_args()

    # Esegui lo script
    main(args.ods, args.token, args.output, args.start_date)

