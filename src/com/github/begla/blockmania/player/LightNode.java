/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.begla.blockmania.player;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class LightNode {

    /**
     * 
     */
    /**
     * 
     */
    /**
     * 
     */
    public int x, y, z;
    private byte _lightIntens = 0;
    private NODE_TYPE _type;

    /**
     * 
     */
    public enum NODE_TYPE {

        /**
         * 
         */
        SPREAD,
        /**
         * 
         */
        UNSPREAD
    }

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @param lightIntens
     * @param t  
     */
    public LightNode(int x, int y, int z, byte lightIntens, NODE_TYPE t) {
        this.x = x;
        this.y = y;
        this.z = z;
        this._lightIntens = lightIntens;
        this._type = t;
    }

    /**
     * 
     * @param lightIntens
     */
    public void setLightIntens(byte lightIntens) {
        this._lightIntens = lightIntens;
    }

    /**
     * 
     * @return
     */
    public byte getLightIntens() {
        return _lightIntens;
    }

    /**
     * 
     * @return
     */
    public NODE_TYPE getType() {
        return _type;
    }

    /**
     * 
     * @param _type
     */
    public void setType(NODE_TYPE _type) {
        this._type = _type;
    }
}