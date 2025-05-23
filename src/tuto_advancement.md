# ---------------TUTO - Advancement---------------

### ---------------commandes---------------
/advancement revoke Dev everything



---

---------------TUTO---------------
=============
1. **Créér un nouveau fichier JSON d'advancement :** _ressources/data/bioreactor/_[advancements](main/resources/data/bioreactor/advancements) 

* voir les recettes déjà existantes.
* L'attribuer à une "famille/un chemin", comme sampling ou cultivation

2. **Enregistrer l'advancement et le trigger:** [BioreactorAdvancement](main/java/net/john/bioreactor/foundation/advancement/BioreactorAdvancements.java)

3. **Déclencher le trigger à un endroit spécifique du code :** ex avec [SamplingEventHandler](main/java/net/john/bioreactor/content/kinetics/Sampling/SamplingEventHandler.java), au moment du "apply" de la recette.
4. **Ajouter une traduction** : fichier langue en_us du Titre et de la description : [en_us](main/resources/assets/bioreactor/lang/en_us.json)

Exemple, advancement selon un **tag (générique, forge-like) de mon mod** :
* Prérequis : avoir listé TOUT les items concernés sous [resources/data/bioreactor/tags](main/resources/data/bioreactor/tags)
* Ecrire le fichier JSON d'advancement comme suit : [exemple](main/resources/data/bioreactor/advancements/sampling_entity_first_time.json)