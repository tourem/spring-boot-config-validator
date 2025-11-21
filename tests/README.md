# Config Preflight - Test Projects

Ce répertoire contient des projets de test pour valider le fonctionnement de `config-preflight` avec différents frameworks Java.

## 📁 Structure

- **spring-boot-test/** - Projet Spring Boot 3.2.0
- **quarkus-test/** - Projet Quarkus 3.16.3
- **micronaut-test/** - Projet Micronaut 4.7.5

## 🎯 Objectif

Chaque projet contient :
- Des classes de configuration avec plusieurs propriétés
- Des fichiers de configuration avec **des propriétés manquantes volontairement**
- Des tests unitaires pour vérifier le chargement des configurations
- Un script `test.sh` pour exécuter les tests avec config-preflight

## 🔍 Propriétés manquantes

Chaque projet teste la détection de propriétés non valorisées dans trois domaines :

### Database Configuration
- ✅ `database.url`
- ✅ `database.username`
- ❌ `database.password` (manquant)
- ✅ `database.max-connections`
- ❌ `database.timeout` (manquant)

### API Configuration
- ❌ `api.endpoint` (manquant)
- ✅ `api.api-key`
- ✅ `api.retry-count`
- ✅ `api.enable-cache`
- ❌ `api.cache-directory` (manquant)

### Messaging Configuration
- ✅ `messaging.broker-url`
- ❌ `messaging.queue-name` (manquant)
- ✅ `messaging.username`
- ✅ `messaging.password`
- ❌ `messaging.connection-timeout` (manquant)
- ✅ `messaging.auto-reconnect`

## 🚀 Utilisation

### Tester un projet spécifique

```bash
# Spring Boot
cd spring-boot-test
./test.sh

# Quarkus
cd quarkus-test
./test.sh

# Micronaut
cd micronaut-test
./test.sh
```

### Tester avec une version spécifique

```bash
# Utiliser une version release
./test.sh 1.0.0

# Utiliser une version snapshot
./test.sh 1.0.1-SNAPSHOT
```

### Tester tous les projets

```bash
# Depuis le répertoire tests/
./test-all.sh

# Avec une version spécifique
./test-all.sh 1.0.0
```

## 📝 Résultats attendus

Config-preflight devrait détecter et rapporter les propriétés manquantes suivantes :

1. **database.password**
2. **database.timeout**
3. **api.endpoint**
4. **api.cache-directory**
5. **messaging.queue-name**
6. **messaging.connection-timeout**

## 🔧 Configuration

Chaque projet utilise la version de config-preflight définie dans son `pom.xml` :

```xml
<config-preflight.version>1.0.0-SNAPSHOT</config-preflight.version>
```

Le script `test.sh` peut mettre à jour cette version dynamiquement :
- Si aucune version n'est spécifiée, il utilise la version du pom.xml
- Si une version est fournie en argument, il met à jour le pom.xml avant de lancer les tests

## 📊 Versions des frameworks

- **Spring Boot** : 3.2.0
- **Quarkus** : 3.16.3 (dernière version stable)
- **Micronaut** : 4.7.5 (dernière version stable)
- **Java** : 17

## 🧪 Tests

Chaque projet contient des tests JUnit 5 qui :
1. Vérifient que les configurations sont bien chargées
2. Testent les propriétés valorisées
3. Documentent les propriétés manquantes (via commentaires)

Les tests devraient **échouer** ou **afficher des warnings** si config-preflight détecte correctement les propriétés manquantes.
