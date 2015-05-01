% Guide des médicaments -- Rapport final
% Aldo Lamarre; Patrice Dumontier-Houle; Charles Desharnais; Guillaume Poirier-Morency
% 30 avril 2012

# Usager cible

Initialement, le projet ciblait le personnel de la santé (infirmières,
médecins, pharmaciens...) afin de servir de « Guide des médicaments » en
version électronique.

Suite à quelques complications, les usagers cibles sont devenus les gens
normaux de la population, c'est-à-dire n'importe quelle personne qui souhaite
faire  usage d'un médicament en raison d'un problème quelconque.  La plupart
des usagers potentiels ont peu de connaissances en informatique. Ils n'ont pas
non-plus la mémoire nécessaire pour se rappeler  de médicaments précédemment
consultés lorsqu'ils naviguent; un usager normal va probablement consulter les
mêmes informations sur le même médicament plus d'une fois. L'usager n'a pas non
plus de connaissances sur les médicaments potentiellement contrindiqués.


# Analyse des besoins

Le besoin principal des usagers est de pouvoir obtenir de l'information sur un
médicament à partir d'une information de départ pas nécessairement précise.

En fonction de l'analyse des usagers, des besoins plus spécifiques ont été
identifiés:

 - Offrir  plusieurs types de recherches (par nom, par ingrédient...) afin de
   maximiser les chances qu'un usager puisse retrouver le médicament souhaité.

 - Offrir un moyen de sauvegarde. Un usager n'a pas une mémoire à court terme
   lui permettant de se rappeler d'informations lues antérieurement sur un
   médicament. Il doit donc y avoir une liste de favoris, contenant les
   médicaments régulièrement consultés.

 - Offrir un panier, afin de permettre à l'usager de pouvoir se faire une liste
   temporaire de médicaments et voir les paires de médicaments contrindiqués.


# Spécification techniques

# Choix de conception

## Consultation de Santé Canada...

On cherche à obtenir de l'information pour développer notre application à
partir des données de Santé Canada.

Après quelques transferts..

 - (613) 941-0839 (Myriam)

# Choix techniques

L'application qui a été développé

 - beaucoup de requête HTTP
 -

Plusieurs librairies ont été utilisées afin de faciliter le développement du
projet.

 - Glide
 - Gson
 - OkHttp


## Glide

[Glide](https://github.com/bumptech/glide) permet d'intégrer des images de
manière efficace dans une application Android.

```java
Glide.with(this)
    .load(images.nlmRxImages[0].imageUrl)
    .into(drugIcon);
```

Elle est utilisé pour charger les images des comprimés associées aux
médicaments. RxNav offre un API pour récupérer des images
[RxImageAccess](http://rximage.nlm.nih.gov/).


## Gson

[Gson](https://github.com/google/gson) est une librairie qui permet d'utiliser
des objects JSON par des classes Java. Il suffit de déclarer une classe qui
représente l'objet fournit par l'API et Gson s'occupe d'en populer une
instance.

L'avantage principal est qu'il n'est plus nécessaire de parcourir la structure
à travers l'API traditionnel avec des `getJSONArray` et `getJSONObject`.


## OkHttp

OkHttp est un client HTTP efficace et résilient.

 - cache
 - compression gzip transparente
 - renvoit les requêtes échouée

L'application utilise une combinaison de fragments et d'activités. Toute la
navigation de l'activité principale se fait à l'aide de fragment.


## Fragments

Les fragments ont l'avantage d'être léger et réutilisable. Le fragment qui
présente un médicament, par exemple, est réutilisé dans l'activité qui présente
un médicament et dans le panier.

La plateforme offre aussi un fragment `ListFragment` pour présenter une liste
de vues équivalent au `ListView`. Il offre deux fonctionnalités intéressante
qui font en sorte qu'il constitue une excellent choix pour présenter des listes
de médicaments:

 - barre de progression lorsque l'adapteur est `null`
 - affiche un message lorsque l'adapteur est vide


# Tests usagers & feedback


# Perspectives d'avenir et rétrospective
