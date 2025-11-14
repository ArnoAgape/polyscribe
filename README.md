<p align="center">
<img src="app/src/main/res/drawable/ic_polyscribe_logo.jpg" alt="Polyscribe Logo" width="200" height="200"/>
</p>

# 🖨️ Polyscribe - Application officielle

**Polyscribe** est une application Android développée pour moderniser les services de **reprographie et d’impression** de l’entreprise **Polyscribe**, située à **Marseille 6ᵉ**.  
Depuis près de **30 ans**, Polyscribe accompagne étudiants, particuliers et professionnels dans leurs besoins en **impression, copie et bureautique**.

L’application permet aux clients d’**envoyer leurs documents directement depuis leur smartphone**, afin de **réduire le temps d’attente** et de **simplifier le processus de commande** en magasin.

---

<div align="center">
   <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
   <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
   <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
   <img src="https://img.shields.io/badge/Android%20Studio-143?style=for-the-badge&logo=android-studio&logoColor=white" alt="Android Studio" />
   <img src="https://img.shields.io/badge/Hilt-3F51B5?style=for-the-badge&logo=dagger&logoColor=white" alt="Hilt" />
   <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor==white" alt="Firebase" /> 
   <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle" />
</div>

---

## 🧠 Objectifs de l’application

- ✅ **Simplifier** la commande d’impression depuis un téléphone
- ✅ **Réduire** le temps de traitement en magasin
- ✅ **Centraliser** les informations clients
- ✅ **Améliorer la communication** entre les utilisateurs et Polyscribe

---

## ⚙️ Architecture & technologies

| Couche                          | Description                                                                                                                      | Technologies principales               |
|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------|----------------------------------------|
| **UI (Interface)**              | Interface 100 % **Jetpack Compose** pour une expérience fluide et moderne                                                        | Compose Material 3, Navigation Compose |
| **Données locales & distantes** | Synchronisation des utilisateurs via **Firebase Firestore**                                                                      | Firebase Firestore, Firebase Auth      |
| **Architecture**                | **MVVM (Model-View-ViewModel)** avec gestion réactive des états                                                                  | ViewModel, StateFlow, MutableStateFlow |
| **Injection de dépendances**    | Gestion automatisée avec **Hilt (Dagger)**                                                                                       | Hilt                                   |
| **Réseau / API interne**        | Connexion à une base Excel existante (id, nom, prénom, téléphone, email, copies restantes, solde) avant migration vers Firestore | Firebase + intégration personnalisée   |
| **Tests**                       | Couverture unitaire et fonctionnelle                                                                                             | JUnit4, Turbine, MockK                 |

---

## 🧩 Fonctionnalités principales (MVP)

- 🔐 Connexion / authentification utilisateur
- 🧾 Consultation du solde et des copies restantes
- 📤 Envoi de documents à imprimer
- 🕓 Historique des commandes
- ⚙️ Synchronisation des données avec Firestore

---

## 🧰 Stack technique

- **Langage** : [Kotlin](https://kotlinlang.org/)
- **UI Toolkit** : [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Architecture** : MVVM + Clean Architecture
- **Base de données** : Firebase Firestore
- **Authentification** : Firebase Auth
- **Injection de dépendances** : Hilt
- **Tests** : JUnit, Turbine
- **Build system** : Gradle (Kotlin DSL)

---

## 👨‍💻 Auteur

Arno Bouiron, développeur Android – passionné d’écologie, d’urbanisme et de mobilité douce 🚴‍♂️

---

## 📜 Licence

Ce projet est développé dans le cadre d’un projet professionnel.
Toute reproduction, modification ou utilisation commerciale non autorisée est interdite sans accord préalable.