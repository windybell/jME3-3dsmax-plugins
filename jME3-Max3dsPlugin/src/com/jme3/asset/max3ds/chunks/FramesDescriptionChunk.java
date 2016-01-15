/**
 * Make a donation http://sourceforge.net/donate/index.php?group_id=98797
 * Microcrowd.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Contact Josh DeFord jdeford@microcrowd.com
 */

package com.jme3.asset.max3ds.chunks;


import com.jme3.asset.max3ds.ChunkChopper;
/**
 * This chunk contains the name of the object
 * the frames belong to and the parameters and 
 * hierarchy information for it.
 */
public class FramesDescriptionChunk extends Chunk
{
    /**
     * reads the name of the object for these frames
     * and stores it in the chopper.
     *
     * @param chopper the chopper used to store the transient data
     * for this chunk. 
     */
    public void loadData(ChunkChopper chopper)
    {
        String objectName = chopper.getString();
        chopper.getUnsignedShort();
        chopper.getUnsignedShort();
        int fatherID = chopper.getShort();
        
        chopper.getCurrentTrack().name = objectName;
        chopper.getCurrentTrack().fatherID = fatherID;
    }
}
