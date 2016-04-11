/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

/**
 *
 * @author Payl
 */
public interface UIInterface {
    public void updateTimer(long t);
    public void updateMap(Map _map);
    public void updateTimerAutoDec(long t);
}
