/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group.thewalkingtec.Interface;

import java.awt.Point;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 *
 * @author diego
 */
public class Soldado extends Thread{
    private boolean isRunning = true;
    private boolean isPause = false;
    private JLabel refLabel;
    private Pantalla refPantalla;
    private int vida = 100;
    private int velocidad;
    
    public Soldado(JLabel refLabel, Pantalla refPantalla){
        this.refLabel = refLabel;
        this.refPantalla = refPantalla;
        this.velocidad = ((new Random()).nextInt(5) + 1) * 1000; // 1000 - 5000
    }
    
    public void run(){
        
        
        while (isRunning){
            try {
                //1. esperar velicidad milisegundos
                sleep(velocidad);
                //2. mover el label aleatoriamente: determinar la posicion: dónde está el objetivo para determinar a dónde debo ir
                Point puntoObjetivo = refPantalla.getObjetivoLocation();
                Point puntoActual = refLabel.getLocation();
                int x = puntoActual.x;
                int y = puntoActual.y;
                if (x < puntoObjetivo.x)
                    x += 20;
                else if (x > puntoObjetivo.x)
                    x -= 20;
                
                if (y < puntoObjetivo.y)
                    y += 20;
                else if (y > puntoObjetivo.y)
                    y -= 20;
                refPantalla.moverSoldado(refLabel, x, y);
                
                //3. pinta el movimiento del label con el método de pantalla
                
                
                
                while (isPause){
                    try {
                        sleep(500);
                    } catch (InterruptedException ex) {
                        //Logger.getLogger(Soldado.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (InterruptedException ex) {
                //Logger.getLogger(Soldado.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public void setPause(){
        this.isPause = !this.isPause;
    }
    
    public void setStop(){
        this.isPause = false;
        this.isRunning = false;
    }
    
    
    
}
