# ---------------TUTO - Item---------------

### ---------------commandes---------------

---

---------------TUTO---------------
=============
1. **enregistrer l'item sous :** [BioreactorItem.java](main/java/net/john/bioreactor/content/item/BioreactorItems.java)

* voir les item déjà existant.
* Cas simple : ItemEntry<Item>
* Cas + complexe : ItemEntry<GlaceItem> avec sa classe dédiée


2. **Créer un fichier model.json:** sous _assets/bioreactor/models/_ [item](main/resources/assets/bioreactor/models/item)
3. **ajouter (ou non) au menu Créatif** : ajouter dans [BioreactorCreativeTab.java](main/java/net/john/bioreactor/BioreactorCreativeTab.java).

   *  ⚠ L'ordre d'enregistrement (haut → bas) est l'ordre d'affichage dans le menu créa.
   * Pour avoir un item + NBT tag particulier dans l'onglet Créa, voir la seringue.
   >output.accept(createTaggedItemStack(new ItemStack(BioreactorItems.SYRINGE_EMPTY.get()), true, true, false));             
   >// +sterile + oxic (- anoxic)


4. **blacklister (ou non) de JEI** : Dans [BioreactorJEIPlugin](main/java/net/john/bioreactor/integration/jei/BioreactorJEIPlugin.java), l'ajouter à la blacklist.

5. **Ajouter le fichier PNG:** sous assets/bioreactor/textures/ [item](main/resources/assets/bioreactor/textures/item)

6. **Texture modifié selon le NBT data tag** :
   * le mettre dans [registerItemProperties](main/java/net/john/bioreactor/content/item/BioreactorItems.java) de BioreactorItem (fonctionne sur les items vanilla)
     * donner un nom à la texture variante, ex syringe_cow_anoxic_texture
   * fichier model n°1 = **texture de base**, [ex, "syringe_cow.json"](main/resources/assets/bioreactor/models/item/syringe_cow.json). Elle spécifie quelle model utiliser selon quel cas.
   * fichier model n°2 = **simple référence au path à utiliser pour trouver la texture 2**. [ex, "syringe_cow_anoxic"](main/resources/assets/bioreactor/models/item/syringe_cow_anoxic.json).

7. **Ajouter une traduction** : fichier langue en_us du Titre et de la description : [en_us](main/resources/assets/bioreactor/lang/en_us.json)
8. **Ajouter (ou non) un tag (générique forge-like)** : Au niveau du package forge, create ou Bioreactoor ([resources/data/bioreactor/tags](main/resources/data/bioreactor/tags))

