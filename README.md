# 📺 Box Monitor

Application Android de surveillance pour Box TV (Android 13).

## Fonctionnalités

- ✅ Capture d'écran automatique toutes les minutes
- ✅ Suivi uptime quotidien (heure début / fin)
- ✅ Service discret en arrière-plan
- ✅ Accès protégé par mot de passe
- ✅ Liste des captures consultable
- ✅ Suppression des captures en un clic
- ✅ Activation / désactivation du monitoring

## Mot de passe

```
Admin123++
```

## Compilation via Codemagic

1. Crée un repo GitHub et push ce projet
2. Connecte-toi sur https://codemagic.io
3. Clique "Add application" → sélectionne ton repo GitHub
4. Codemagic détecte automatiquement le fichier `codemagic.yaml`
5. Lance un build → récupère l'APK dans les artifacts

## Installation sur la Box TV

1. Active "Sources inconnues" dans les paramètres Android
2. Transfère l'APK via clé USB ou ADB :
   ```
   adb install app-debug.apk
   ```
3. Lance l'app, entre le mot de passe `Admin123++`
4. Active la surveillance et autorise la capture d'écran

## Structure du projet

```
BoxMonitor/
├── app/src/main/
│   ├── java/com/boxmonitor/
│   │   ├── data/          # Modèles, UptimeManager, ScreenshotManager
│   │   ├── service/       # MonitoringService, BootReceiver
│   │   └── ui/            # Activities, Adapters
│   └── res/               # Layouts, strings, themes
├── codemagic.yaml          # Config build cloud
└── build.gradle
```

## Notes techniques

- Requiert Android 11+ (API 30)
- Utilise MediaProjection API pour les captures
- La permission capture doit être accordée manuellement au premier lancement
- Les screenshots sont stockés dans le dossier externe de l'app
