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


## Cas typique

L'infirmière analyse un patient, construit un diagnostic et trouve un
médicament applicable.

1. Diagnostic

 - le patient a besoin d'un tel médicament
 - catégorie de médicament (class pour l'api)
 - médicament en particulier (avec expérience)

2. Recherche

L'infirmière effectue une recherche pour identifier un médicament ou une
catégorie.

Cherche à obtenir

 - le nom du médicament
 - la catégorie
 - la posologie
 - méthode d'administration (sous-cutané, injection IV, oral, _anal_)
 - la couleur et la forme du médicament


# Analyse des besoinsa

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

L'analyse décrit un cas typique d'utilisation de l'application
« Guide des médicaments » et énumère les besoins en interface usager.

Le choix se fait à travers une liste de médicaments.

 - allergies
 - contre-indications
 - type d'administation
 - indice maternité
 - forme et couleur
    - voir la terminologie pour les formes
 - catégories
    - plusieurs catégories par médicament

4. Affichage du médicament

 - nom du médicament
 - catégories
 - forme et couleur
 - type d'administation
 - indice maternité
 - contre-indications
 - posologie
 - description complète
    - voir plus
 - médicaments génériques et similaires

# Autres idées

 - image du médicament
 - comparaison de plusieurs médicaments
 - panier de médicament
    - vérifie dans une recherche les médicaments en conflit
    - permet de
    - paniers sauvegardés

# Besoins en interface usager

 - interface de recherche (base de l'application)
    - dernières recherches de médicament
    - liste des paniers
 - liste de médicaments
    - par catégorie (class)
    - par recherche
 - information d'un médicament
 - panier
    - consulter chaque information en glissant latéralement

# Consultation de Santé Canada...

On cherche à obtenir de l'information pour développer notre application à
partir des données de Santé Canada.

Après quelques transferts..

 - (613) 941-0839 (Myriam)

# Spécification techniques

# Choix de conception

# Choix techniques

# Tests usagers & feedback


# Perspectives d'avenir et rétrospective



Choix techniques
----------------









