/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group.thewalkingtec.Interface;

import javax.swing.JLabel;


public class Objetivo {
    private JLabel refLabel;
    int vida = 1000;

    public Objetivo(JLabel refLabel) {
        this.refLabel = refLabel;
    }
    
    public int recibirAtaque(){
        return --vida;
    }

    public JLabel getRefLabel() {
        return refLabel;
    }

    public void setRefLabel(JLabel refLabel) {
        this.refLabel = refLabel;
    }

    public int getVida() {
        return vida;
    }

    public void setVida(int vida) {
        this.vida = vida;
    }
    
    
}
