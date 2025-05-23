package net.john.bioreactor.content.kinetics.axenisation;

public interface IMechanicalMixer {
    void forcePoleDown();  // force la position basse sans redémarrer l'animation
    void releasePole();    // laisse l'animation remonter naturellement
}
