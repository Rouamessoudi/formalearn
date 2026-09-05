package com.esprit.formation.config;

import com.esprit.formation.catalogue.domain.Category;
import com.esprit.formation.catalogue.domain.Chapter;
import com.esprit.formation.catalogue.domain.Formation;
import com.esprit.formation.catalogue.repository.CategoryRepository;
import com.esprit.formation.catalogue.repository.ChapterRepository;
import com.esprit.formation.catalogue.repository.FormationRepository;
import com.esprit.formation.common.domain.EducationLevel;
import com.esprit.formation.common.domain.EnrollmentStatus;
import com.esprit.formation.common.domain.FormationStatus;
import com.esprit.formation.common.domain.InterestArea;
import com.esprit.formation.common.domain.RoleName;
import com.esprit.formation.common.domain.SessionStatus;
import com.esprit.formation.enrollment.domain.Enrollment;
import com.esprit.formation.enrollment.repository.EnrollmentRepository;
import com.esprit.formation.session.domain.TrainingSession;
import com.esprit.formation.session.repository.TrainingSessionRepository;
import com.esprit.formation.user.domain.Profile;
import com.esprit.formation.user.domain.Role;
import com.esprit.formation.user.domain.User;
import com.esprit.formation.user.repository.RoleRepository;
import com.esprit.formation.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DemoDataInitializer implements ApplicationRunner {

    public static final String ADMIN_EMAIL = "admin@formalearn.tn";
    public static final String LEARNER_EMAIL = "apprenant@formalearn.tn";
    public static final String LEARNER2_EMAIL = "apprenant2@formalearn.tn";
    public static final String LEARNER3_EMAIL = "apprenant3@formalearn.tn";

    private static final String SPRING_API = "Spring Boot — Développement d'API REST";
    private static final String DOCKER = "Docker et Docker Compose";
    private static final String ANGULAR = "Développement Web Moderne avec Angular";
    private static final String MYSQL = "MySQL — De débutant à avancé";
    private static final String JENKINS = "CI/CD avec Jenkins";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final FormationRepository formationRepository;
    private final ChapterRepository chapterRepository;
    private final TrainingSessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;

    public DemoDataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CategoryRepository categoryRepository,
            FormationRepository formationRepository,
            ChapterRepository chapterRepository,
            TrainingSessionRepository sessionRepository,
            EnrollmentRepository enrollmentRepository
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.formationRepository = formationRepository;
        this.chapterRepository = chapterRepository;
        this.sessionRepository = sessionRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role adminRole = ensureRole(RoleName.ADMIN);
        Role learnerRole = ensureRole(RoleName.APPRENANT);
        upsertUser(ADMIN_EMAIL, "Admin123!", "Roua Admin", adminRole, false, null);
        upsertUser(LEARNER_EMAIL, "Learner123!", "Roua Messaoudi", learnerRole, true,
                new DemoProfile(InterestArea.BACKEND, 3, EducationLevel.INGENIEUR, "JAVA,SPRING,SQL"));
        upsertUser(LEARNER2_EMAIL, "Learner123!", "Sami Trabelsi", learnerRole, true,
                new DemoProfile(InterestArea.DATA, 2, EducationLevel.MASTER, "PYTHON,SQL"));
        upsertUser(LEARNER3_EMAIL, "Learner123!", "Ines Gharbi", learnerRole, true,
                new DemoProfile(InterestArea.BACKEND, 1, EducationLevel.LICENCE, "JAVA,SQL"));
        retireLegacyDemoCatalogue();
        seedCatalogue();
        seedSessions();
        seedEnrollments();
    }

    /**
     * Retire l'ancien jeu de démo (catégories Informatique / Data Science / Business / Langues)
     * pour que le jour de validation n'affiche que les 6 thèmes demandés.
     */
    private void retireLegacyDemoCatalogue() {
        for (String name : new String[]{"Informatique", "Data Science", "Business", "Langues"}) {
            categoryRepository.findByNameIgnoreCase(name).ifPresent(category -> {
                formationRepository.search(false, FormationStatus.PUBLISHED, category.getId(), null, null, null)
                        .forEach(this::deleteFormationCascade);
                if (!formationRepository.existsByCategoryId(category.getId())) {
                    categoryRepository.delete(category);
                }
            });
        }
    }

    private void deleteFormationCascade(Formation formation) {
        sessionRepository.findByFormationIdWithFormation(formation.getId()).forEach(session -> {
            enrollmentRepository.findBySessionIdWithDetails(session.getId()).forEach(enrollmentRepository::delete);
            sessionRepository.delete(session);
        });
        chapterRepository.findByFormationIdOrderByPositionAsc(formation.getId()).forEach(chapterRepository::delete);
        formationRepository.delete(formation);
    }

    private void seedCatalogue() {
        Category web = ensureCategory("Développement Web", "Front-end, TypeScript et frameworks SPA.");
        Category java = ensureCategory("Java & Spring", "Langage Java, Spring Boot et architectures d'API.");
        Category data = ensureCategory("Bases de données", "Modélisation, SQL et optimisation MySQL.");
        Category devops = ensureCategory("DevOps & Cloud", "Conteneurisation, CI/CD et introduction au cloud.");
        Category ia = ensureCategory("Intelligence Artificielle", "Machine learning appliqué et Python data.");
        Category cyber = ensureCategory("Cybersécurité", "Sécurité des API, des identités et des applications web.");

        ensureCourse(ANGULAR, "Construire une application Angular professionnelle : routing, guards JWT et consommation d'une API Spring.",
                "420.00", 28, web, FormationStatus.PUBLISHED,
                "Composants standalone",
                "Objectif : arrêter les NgModules pour une app Angular 18. J’ai découpé FormaLearn en pages standalone (login, catalogue, dashboard). Chaque composant importe ReactiveFormsModule ou RouterLink tout seul. À retenir : moins de boilerplate, compilation plus claire pour la soutenance.",
                "Routing et guards",
                "Objectif : empêcher un apprenant d’ouvrir /admin. J’ai posé adminGuard / learnerGuard / guestGuard dans app.routes.ts. Cas testé : token absent → login ; rôle APPRENANT sur une route ADMIN → redirection. C’est la partie « Angular avancé » que le professeur veut voir.",
                "HTTP et interceptors",
                "Objectif : coller le JWT sur chaque appel sans le copier à la main. authInterceptor lit localStorage, ajoute Authorization Bearer, et si Spring répond 401 je déconnecte. J’ai aussi withCredentials: false pour coller au CORS Spring (pas de cookies).",
                "Formulaires réactifs",
                "Objectif : valider avant d’appeler l’API. Catégorie : nom obligatoire. Login : email + mot de passe min 6. Le bouton Enregistrer reste désactivé tant que le FormGroup est invalid. Message d’erreur sous le champ, toast si Spring renvoie 400.",
                "Mise en production",
                "Objectif : ng build puis Nginx. environment.prod.ts utilise /api pour que le navigateur passe par le reverse proxy Docker, pas par localhost:8080. Budget de bundle et try_files pour le routing Angular.",
                "TP perso — page Catégories",
                "J’ai testé le CRUD complet : créer, modifier, supprimer. Point dur : 401 si l’ancien token reste après un redémarrage Spring. Solution : déconnexion puis login, interceptor 401. C’est le TP que je montrerai en live.");

        ensureCourse("TypeScript de zéro à avancé", "Maîtriser les types, les génériques et la conception d'un front robuste.",
                "280.00", 16, web, FormationStatus.PUBLISHED,
                "Types de base",
                "Interfaces Category / Formation alignées sur les DTO Spring. Unions DRAFT | PUBLISHED pour ne pas écrire de string magique. Si le backend change un champ, le compilateur TypeScript le voit avant la démo.",
                "Génériques",
                "HttpClient.get<Category[]>() : le tableau du dashboard est typé. Moins d’erreurs any. Observable<T> sur tous les services api.",
                "Modules",
                "Découpage frontend/src/app/core (auth, api) et features (admin, learner). Pas de module NgModule : tout est standalone, comme demandé en Angular moderne.",
                "Bonnes pratiques",
                "strict templates, pas de logique métier dans le HTML. Les règles 409 (doublon, session pleine) restent côté Spring ; le front affiche le message JSON.");

        ensureCourse("React.js pour applications modernes", "Hooks, composition et état — formation laissée en brouillon pour la démo DRAFT.",
                "390.00", 24, web, FormationStatus.DRAFT,
                "JSX et composants",
                "Brouillon : comparer JSX et les templates Angular. Je laisse DRAFT pour montrer qu’un APPRENANT ne voit pas cette formation dans le catalogue.",
                "Hooks",
                "useState / useEffect vs signals Angular. Note perso : je ne l’ai pas branché dans FormaLearn, c’est hors stack du PI.",
                "Routage",
                "React Router vs app.routes.ts. Gardé en DRAFT volontairement.",
                "État global",
                "Contexte React vs AuthService + signals. Brouillon pédagogique.");

        ensureCourse("Java 17 — Programmation avancée", "Records, sealed types, collections et API Stream pour un backend d'entreprise.",
                "350.00", 22, java, FormationStatus.PUBLISHED,
                "Nouveautés Java 17",
                "Records pour des DTO immuables (ex. DemoProfile dans le seed). Pattern matching dans les switch de mapping catégorie → INFORMATIQUE / DATA_SCIENCE pour le MLA.",
                "Collections",
                "List<Formation> publiées seulement pour l’apprenant. Map id → formation dans RecommendationService. Pas de logique dans le contrôleur.",
                "Streams",
                "filter(status == PUBLISHED), sorted par score ML, limit(topN). C’est du Java avancé utilisé pour de vrai dans /api/mla/recommandations.",
                "Exceptions",
                "ApiException + @ControllerAdvice : 404 formation, 409 session pleine. Le front lit error.message. Pas de stack HTML.",
                "Tests unitaires",
                "JUnit 5 + MockMvc : 27 tests d’intégration (auth, catalogue, inscriptions, MLA). Un test rouge fait échouer Maven et Jenkins.");

        ensureCourse(SPRING_API, "Exposer une API REST sécurisée : contrôleurs, JPA, validation et JWT — socle de FormaLearn.",
                "490.00", 32, java, FormationStatus.PUBLISHED,
                "Projet Spring Boot",
                "Dépendances web, security, jpa, mysql, validation. Profils dev / test. application.yml : JWT_SECRET, ML_BASE_URL. Structure par package métier (catalogue, session, enrollment, mla) sans microservices.",
                "REST et DTOs",
                "CategoryRequest avec @NotBlank, FormationResponse, SessionResponse.remainingPlaces. Codes : 201 create, 204 delete, 400 validation, 401 JWT, 403 rôle, 404, 409 conflit. C’est la partie Spring avancée du PI.",
                "JPA et MySQL",
                "Entités Category 1—N Formation 1—N Chapter, Formation 1—N TrainingSession 1—N Enrollment. Unique (user, session). Repositories avec JOIN FETCH pour éviter le N+1.",
                "Sécurité JWT",
                "JwtAuthenticationFilter dans SecurityFilterChain seulement (pas de double filtre Tomcat). hasRole ADMIN sur POST /api/categories. APPRENANT interdit. BCrypt sur les mots de passe de démo.",
                "Tests d'intégration",
                "MockMvc login admin puis POST catégorie, GET apprenant sans DRAFT, inscription 409 si doublon. Ça prouve les règles métier sans cliquer.",
                "TP perso — capacité de session",
                "J’ai verrouillé findByIdForUpdate au moment de l’inscription. Si remainingPlaces = 0 → 409 « session complète ». L’admin qui passe une inscription en CANCELLED libère une place. Cas que je montrerai sur la session Spring d’octobre (capacité 3).");

        ensureCourse("Architecture Microservices avec Spring", "Découpage, communication et observabilité — hors périmètre du PI, laissé en DRAFT.",
                "720.00", 40, java, FormationStatus.DRAFT,
                "Bounded contexts",
                "DRAFT : FormaLearn reste un monolithe modulaire. J’explique au professeur pourquoi je n’ai pas découpé en 5 jars.",
                "API Gateway",
                "Note : Nginx /api vers backend dans Docker joue un rôle de façade, ce n’est pas Spring Cloud Gateway.",
                "Résilience",
                "Timeouts RestClient vers FastAPI (2 s connect, 5 s read). 503 si le MLA est down.",
                "Observabilité",
                "Logs Hibernate en dev. Health Spring + health FastAPI. Brouillon volontaire.");

        ensureCourse(MYSQL, "Installer, modéliser et interroger MySQL pour une application Spring.",
                "260.00", 14, data, FormationStatus.PUBLISHED,
                "Modèle relationnel",
                "Tables categories, formations, chapters, training_sessions, enrollments, users, profiles, roles. FK et uk_chapter_formation_position pour l’ordre du programme.",
                "CRUD SQL",
                "ddl-auto=update en démo. Seed idempotent : findByNameIgnoreCase avant insert. Pas de doublon à chaque restart.",
                "Jointures",
                "Inscriptions : user + session + formation + category en une requête JPQL. Le tableau Mes inscriptions n’est pas N+1.",
                "Indexes",
                "Recherche LOWER(title) LIKE %q%. Prix min/max. Filtre catégorie. C’est le search du catalogue apprenant.");

        ensureCourse("SQL avancé et optimisation des requêtes", "GROUP BY, fenêtres et plans d'exécution pour le reporting.",
                "310.00", 18, data, FormationStatus.PUBLISHED,
                "Agrégations",
                "Dashboard admin : COUNT formations, SUM PUBLISHED. countActiveBySessionId ignore CANCELLED pour remainingPlaces.",
                "Sous-requêtes",
                "existsByUserIdAndSessionId pour refuser le doublon. existsByFormationId pour interdire de supprimer une formation qui a des sessions.",
                "Fenêtres",
                "Idée d’évolution : RANK des formations par nombre d’inscrits. Non livré pour rester dans le périmètre 2 modules.",
                "EXPLAIN",
                "En démo on montre plutôt les requêtes Hibernate formatées. Index sur email unique des users.");

        ensureCourse(DOCKER, "Dockerfiles multi-stages, Compose, réseaux et healthchecks mysql / backend / ml / frontend.",
                "380.00", 16, devops, FormationStatus.PUBLISHED,
                "Images applicatives",
                "Backend JRE 17, frontend Nginx, ML Python slim. Multi-stage Maven / npm pour des images petites.",
                "Réseau Compose",
                "MYSQL_HOST=mysql, ML_BASE_URL=http://ml:8000, proxy_pass http://backend:8080. Jamais localhost entre conteneurs.",
                "Volumes",
                "mysql_data pour ne pas perdre le seed. Le jour J : docker compose up -d puis http://localhost:8088.",
                "Healthchecks",
                "depends_on condition service_healthy. Backend curl /api/health. Ordre mysql → ml → backend → frontend.");

        ensureCourse(JENKINS, "Pipeline déclarative : compile, tests, entraînement ML, package et docker build.",
                "450.00", 20, devops, FormationStatus.PUBLISHED,
                "Jenkinsfile",
                "Stages Checkout, Backend Build/Tests, Frontend Build/Tests, ML Tests, ML Training, Sonar skippable, Package, Docker Build. FORCE_FAIL pour montrer un pipeline rouge.",
                "Maven et npm",
                "mvnw -B test : 27 tests. ng build. Un échec arrête le pipeline, on ne force pas SUCCESS.",
                "Credentials",
                "Aucun mot de passe dans Git. JWT_SECRET et MYSQL_ROOT_PASSWORD dans .env / Jenkins credentials.",
                "Docker build",
                "Images formalearn-backend/frontend/ml taguées BUILD_NUMBER.",
                "Quality gate",
                "SKIP_SONAR=true tant qu’il n’y a pas de serveur Sonar. Stage explicite « skipped », pas un fake vert.");

        ensureCourse("Introduction au Cloud Computing", "IaaS, PaaS et bonnes pratiques de secrets, sans Kubernetes obligatoire.",
                "290.00", 12, devops, FormationStatus.PUBLISHED,
                "Modèles de cloud",
                "Docker Compose = PaaS de démo locale. On n’impose pas K8s pour le PI.",
                "Identité",
                "Comptes admin / apprenant séparés. JWT 8 h. Pas de session serveur.",
                "Secrets",
                "Variables d’environnement Spring. .env.example documenté, .env non commité.",
                "Coûts",
                "Un laptop + XAMPP ou Docker. Suffisant pour la validation.");

        ensureCourse("Introduction au Machine Learning", "Problème supervisé, train/test, métriques accuracy precision recall F1.",
                "360.00", 18, ia, FormationStatus.PUBLISHED,
                "Problème ML",
                "Classification binaire : recommander ou non une formation à un profil. Pas de NLP, pas de TF-IDF (consigne du sujet).",
                "Dataset",
                "CSV tabulaire généré : intérêt, années d’expérience, niveau, skills, catégorie, prix, durée.",
                "Split",
                "Train/test dans train.py. Pas de fuite : le modèle ne voit pas les labels test.",
                "Métriques",
                "On choisit le meilleur F1. Random Forest : F1 0.8475, accuracy 0.8313.");

        ensureCourse("Machine Learning avec Python", "scikit-learn : Logistic Regression, Tree, KNN, Random Forest et pipeline.",
                "520.00", 28, ia, FormationStatus.PUBLISHED,
                "Pandas",
                "Chargement du CSV, colonnes FEATURE_COLUMNS partagées entre generate, train et FastAPI.",
                "Preprocessing",
                "Pipeline StandardScaler + OneHotEncoder. Même pipeline sérialisé dans recommender_model.pkl.",
                "Comparaison",
                "Quatre modèles entraînés, tableau metrics.json. Aucun score inventé.",
                "Sélection",
                "Random Forest retenu sur le F1 test.",
                "Joblib",
                "Sauvegarde pkl. FastAPI /predict. Spring RestClient. Angular /app/recommandations.");

        ensureCourse("Intelligence Artificielle appliquée", "Cas d'usage avancés NLP — brouillon pour montrer un DRAFT côté admin.",
                "610.00", 30, ia, FormationStatus.DRAFT,
                "Collecte",
                "DRAFT : je n’utilise pas de vrais CV d’étudiants. Dataset synthétique uniquement.",
                "Modèles",
                "Limite : un Random Forest tabulaire n’est pas du deep learning. Je l’assume en soutenance.",
                "Mise en prod",
                "FastAPI charge le modèle au startup pour éviter un 502 au premier appel.",
                "Gouvernance",
                "Recommandations expliquées par le profil (JAVA, SPRING, SQL), pas une liste hardcodée.");

        ensureCourse("Fondamentaux de la cybersécurité", "Menaces, CIA, identités et bonnes pratiques pour une équipe de développement.",
                "270.00", 12, cyber, FormationStatus.PUBLISHED,
                "CIA",
                "Confidentialité : JWT. Intégrité : validation Bean Validation. Disponibilité : healthchecks Compose.",
                "Menaces",
                "Pas de mot de passe en clair (BCrypt). Pas de secret dans le Jenkinsfile.",
                "Identités",
                "Deux rôles seulement, comme le sujet. Compte démo documenté dans le README.",
                "Réponse",
                "401 / 403 JSON. L’UI ne reste plus « connectée » sur un token mort : interceptor → login.");

        ensureCourse("Sécurité des applications Web", "JWT, BCrypt, CORS, 401/403 et validation des entrées — tel que dans FormaLearn.",
                "410.00", 18, cyber, FormationStatus.PUBLISHED,
                "Authentification",
                "POST /api/auth/login → token. Mauvais mot de passe 401. Expiration 8 h.",
                "Autorisations",
                "ADMIN crée les catégories. APPRENANT s’inscrit. Testé : 403 sur /api/admin/ping.",
                "Mots de passe",
                "BCrypt. Comptes seed ré-encodés au démarrage avec Admin123! / Learner123!.",
                "Attaques courantes",
                "Paramètres typés, pas de concat SQL. @Valid sur les body. Positions de chapitres uniques.",
                "Headers",
                "CORS origins 4200 et 8088, Authorization autorisé, allowCredentials false.");
    }

    private void seedSessions() {
        Formation spring = title(SPRING_API);
        Formation docker = title(DOCKER);
        Formation angular = title(ANGULAR);
        Formation mysql = title(MYSQL);
        Formation jenkins = title(JENKINS);

        ensureSession(spring, LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 30), 20, SessionStatus.OPEN);
        ensureSession(spring, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 20), 3, SessionStatus.OPEN);
        ensureSession(spring, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 17), 16, SessionStatus.OPEN);
        ensureSession(docker, LocalDate.of(2026, 9, 20), LocalDate.of(2026, 10, 4), 12, SessionStatus.OPEN);
        ensureSession(angular, LocalDate.of(2026, 9, 22), LocalDate.of(2026, 10, 10), 18, SessionStatus.OPEN);
        ensureSession(mysql, LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 26), 15, SessionStatus.CLOSED);
        ensureSession(jenkins, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 15), 25, SessionStatus.OPEN);
        ensureSession(jenkins, LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 22), 14, SessionStatus.OPEN);
    }

    private void seedEnrollments() {
        LocalDate springA = LocalDate.of(2026, 9, 15);
        LocalDate springFull = LocalDate.of(2026, 10, 5);
        LocalDate docker = LocalDate.of(2026, 9, 20);
        enroll(LEARNER_EMAIL, SPRING_API, springA, EnrollmentStatus.CONFIRMED);
        enroll(LEARNER_EMAIL, DOCKER, docker, EnrollmentStatus.PENDING);
        enroll(LEARNER2_EMAIL, SPRING_API, springA, EnrollmentStatus.PENDING);
        enroll(LEARNER_EMAIL, SPRING_API, springFull, EnrollmentStatus.PENDING);
        enroll(LEARNER2_EMAIL, SPRING_API, springFull, EnrollmentStatus.CONFIRMED);
    }

    private Category ensureCategory(String name, String description) {
        return categoryRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            return categoryRepository.save(category);
        });
    }

    private void ensureCourse(
            String title,
            String description,
            String price,
            int hours,
            Category category,
            FormationStatus status,
            String... chapterPairs
    ) {
        Formation formation = formationRepository.findFirstByTitle(title).orElseGet(() -> {
            Formation created = new Formation();
            created.setTitle(title);
            created.setDescription(description);
            created.setPrice(new BigDecimal(price));
            created.setDurationHours(hours);
            created.setCategory(category);
            created.setStatus(status);
            return formationRepository.save(created);
        });
        upsertChapters(formation, chapterPairs);
    }

    private void upsertChapters(Formation formation, String... chapterPairs) {
        int position = 1;
        for (int i = 0; i < chapterPairs.length; i += 2) {
            final int pos = position;
            Chapter chapter = chapterRepository.findByFormationIdAndPosition(formation.getId(), pos)
                    .orElseGet(() -> {
                        Chapter created = new Chapter();
                        created.setFormation(formation);
                        created.setPosition(pos);
                        return created;
                    });
            chapter.setTitle(chapterPairs[i]);
            chapter.setContent(chapterPairs[i + 1]);
            chapterRepository.save(chapter);
            position++;
        }
    }

    private void ensureSession(Formation formation, LocalDate start, LocalDate end, int capacity, SessionStatus status) {
        if (sessionRepository.existsByFormationIdAndStartDate(formation.getId(), start)) {
            return;
        }
        TrainingSession session = new TrainingSession();
        session.setFormation(formation);
        session.setStartDate(start);
        session.setEndDate(end);
        session.setCapacity(capacity);
        session.setStatus(status);
        sessionRepository.save(session);
    }

    private void enroll(String email, String formationTitle, LocalDate start, EnrollmentStatus status) {
        User user = userRepository.findByEmailIgnoreCaseWithRole(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur de démo introuvable : " + email));
        TrainingSession session = sessionRepository.findByFormationIdWithFormation(title(formationTitle).getId()).stream()
                .filter(item -> start.equals(item.getStartDate()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Session de démo introuvable : " + formationTitle + " " + start));
        if (enrollmentRepository.existsByUserIdAndSessionId(user.getId(), session.getId())) {
            return;
        }
        long active = enrollmentRepository.countActiveBySessionId(session.getId());
        if (status != EnrollmentStatus.CANCELLED && active >= session.getCapacity()) {
            return;
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setSession(session);
        enrollment.setStatus(status);
        enrollmentRepository.save(enrollment);
    }

    private Formation title(String title) {
        return formationRepository.findFirstByTitle(title)
                .orElseThrow(() -> new IllegalStateException("Formation de démo introuvable : " + title));
    }

    private Role ensureRole(RoleName name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            return roleRepository.save(role);
        });
    }

    private void upsertUser(String email, String rawPassword, String fullName, Role role, boolean withProfile, DemoProfile demoProfile) {
        User user = userRepository.findByEmailIgnoreCaseWithRole(email).orElseGet(User::new);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setRole(role);
        if (withProfile && demoProfile != null) {
            Profile profile = user.getProfile();
            if (profile == null) {
                profile = new Profile();
                user.setProfile(profile);
            }
            profile.setInterest(demoProfile.interest());
            profile.setExperienceYears(demoProfile.experienceYears());
            profile.setEducationLevel(demoProfile.educationLevel());
            profile.setSkillTags(demoProfile.skillTags());
        }
        userRepository.save(user);
    }

    private record DemoProfile(InterestArea interest, int experienceYears, EducationLevel educationLevel, String skillTags) {
    }
}
