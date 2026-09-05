# FormaLearn — chaîne DevOps

Dépôt GitHub central : https://github.com/Rouamessoudi/formalearn

## 1. Architecture

```
GitHub (source)
  → Jenkins (CI)
      → tests Maven / Karma / pytest
      → JaCoCo + SonarQube Quality Gate
      → docker build (+ push optionnel)
      → kubectl apply (si DEPLOY_K8S)
Docker Compose (démo soutenance)
  → frontend :8088 → backend → MySQL + FastAPI
  → Actuator → Prometheus :9091 → Grafana :3000
```

L’application métier **n’est pas** Jenkins. Démo LMS : `http://localhost:8088`.

## 2. GitHub

Clone :

```bash
git clone https://github.com/Rouamessoudi/formalearn.git
cd formalearn
cp .env.example .env
```

Ne jamais committer `.env`.

## 3. Jenkins

```bash
cd jenkins
docker compose up -d --build
```

UI : http://localhost:9090 — `admin` / `change_me_jenkins` (volume neuf).

Job recommandé : **Pipeline script from SCM** → Git → `https://github.com/Rouamessoudi/formalearn.git` → `Jenkinsfile` → branche `main`.

Credentials Jenkins (Secret text / username-password, jamais dans Git) :

| ID | Usage |
|---|---|
| `sonar-token` | Token utilisateur SonarQube |
| `docker-registry` | Login registry si `PUSH_IMAGES` |

Variable globale : `SONAR_HOST_URL` = `http://host.docker.internal:9000` (Sonar Compose) ou URL réelle.

Webhook GitHub → Jenkins (machine **publique** ou tunnel) :

1. Jenkins → Manage → système → URL Jenkins joignable depuis Internet **ou** ngrok/cloudflared.
2. GitHub repo → Settings → Webhooks → `http://<jenkins-public>/github-webhook/` → events **push**.
3. Job : cocher **GitHub hook trigger for GITScm polling**.

Sans URL publique, GitHub **ne peut pas** appeler `localhost:9090`. Utiliser **Build Now** après `git push`.

## 4. SonarQube

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d sonarqube
```

UI : http://localhost:9000 (premier login `admin`/`admin`, puis changer le mot de passe).

Créer un token, le coller dans Jenkins `sonar-token`.

Quality Gate : stage Jenkins `waitForQualityGate`. Configurer dans SonarQube un webhook vers `http://<jenkins>/sonarqube-webhook/`.

## 5. Docker (app)

```bash
docker compose up --build -d
```

Vérifier : http://localhost:8088 — health `http://localhost:8082/api/health` si `BACKEND_HOST_PORT=8082`.

## 6. Kubernetes

`kubectl` est présent ici, mais **aucun cluster sain** n’a répondu (`cluster-info` HTTP 500). Les manifests sont dans `k8s/`.

À faire sur une machine avec **kind**, **k3d** ou **Docker Desktop Kubernetes** activé et **healthy** :

```bash
cp k8s/secrets.example.yaml k8s/secrets.local.yaml
# éditer les mots de passe, ne pas committer
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.local.yaml
kubectl apply -k k8s/
```

Images `formalearn-*:local` doivent exister sur le nœud (`kind load docker-image` si kind).

## 7. Prometheus / Grafana

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d
```

- Prometheus : http://localhost:9091 — target `formalearn-backend` UP  
- Grafana : http://localhost:3000 — `admin` / `admin` (ou `GRAFANA_ADMIN_PASSWORD`)  
- Dashboard provisionné : **FormaLearn — Spring Boot**

Actuator (après rebuild backend) :

- http://localhost:8082/actuator/health  
- http://localhost:8082/actuator/prometheus  

## 8. How to verify

| Service | Commande / URL |
|---|---|
| GitHub | https://github.com/Rouamessoudi/formalearn |
| FormaLearn | http://localhost:8088 |
| API health | `curl http://localhost:8082/api/health` |
| Actuator | `curl http://localhost:8082/actuator/health` |
| ML | `curl http://localhost:8000/health` |
| Jenkins | http://localhost:9090 |
| SonarQube | http://localhost:9000 |
| Prometheus | http://localhost:9091/targets |
| Grafana | http://localhost:3000 |
| Kubernetes | `kubectl -n formalearn get pods` (si cluster OK) |

## 9. Démo soutenance (12 min)

1. GitHub : montrer le repo `main`.  
2. `docker compose ps` : 4 services healthy, ouvrir :8088.  
3. Optionnel : Grafana :3000.  
4. Jenkins : pipeline from SCM, build (SKIP_SONAR si Sonar pas encore token).  
5. Dire : K8s YAML prêts ; cluster local non opérationnel sur ce PC.
