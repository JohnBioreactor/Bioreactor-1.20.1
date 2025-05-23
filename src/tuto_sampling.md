# ---------------TUTO - Sampling---------------

### ---------------commandes---------------
/give @p minecraft:glass_bottle{sterile:1b} 1

/give @p minecraft:glass_bottle{sterile:1b,oxic:1b} 1

/give @p minecraft:glass_bottle{sterile:1b,anoxic:1b} 1

.

/give @p bioreactor:syringe_empty{sterile:1b} 1

/give @p bioreactor:syringe_empty{sterile:1b,oxic:1b} 1

/give @p bioreactor:syringe_empty{sterile:1b,anoxic:1b} 1



---

### ---------------fonctionnement de la recette---------------
* **Entrée** : seringue ou bouteille vide, **_⚠ avec_** le tag #sterile.  
    * seringue utilisée uniquement si c'est un mob
    * bouteille vide utilisée uniquement si c'est un block ou un fluide
* **Cible** : block, fluid ou entité
* **Context** : possibilité de spécifier
    * La hauteur (Ymin, Ymax) de prélevement
    * Le NBT tag du block prélevé
    * La présence de block ou de fluide au dessus du block ciblé.
      * le nombre 'threshold'
      * chacun doit avoir le NBT tag xx.
* **Sécurité**, Ne pas casser le jeu de base : mixin [BottleItemMixin](main/java/net/john/bioreactor/content/mixin/watervanilla/BottleItemMixin.java) fait que si la bouteille n'a PAS le NBT tag #sterile, comportement vanilla.

---

---------------TUTO---------------
=============
1. **Créér un nouveau fichier JSON de recette de sampling** : [lien](main/resources/data/bioreactor/recipes/sampling)

voir les recettes déjà existantes.


2. **Créér l'item et sa texture samplé** : voir [tuto créer un item](tuto_item.md)
3. **Ajouter un tag générique**
   * Pour les bouteille : l'ajouter dans [sample_block_fluid.json](main/resources/data/bioreactor/tags/items/sample_block_fluid.json)
   * Pour les seringues : l'ajouter dans [sample_microbiote.json](main/resources/data/bioreactor/tags/items/sample_microbiote.json)