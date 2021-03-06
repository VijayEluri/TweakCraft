package com.tweakcraft.server.instancemanager;

import com.tweakcraft.server.instance.Player;
import com.tweakcraft.server.model.Block;
import com.tweakcraft.server.model.Chunk;
import gnu.trove.TLongObjectHashMap;
import java.util.Collection;
import javolution.util.FastMap;

/**
 *
 * @author Meaglin
 */
public class World {

    private final TLongObjectHashMap<Chunk> _chunks = new TLongObjectHashMap<Chunk>();
    private final FastMap<Integer, Player> _players = new FastMap<Integer,Player>().shared();;

    private World() {
    }


    public Chunk getChunk(int x,int y){
	synchronized(_chunks){
	    int chunkx = x >> 6, chunky = y >> 6;
	    long location = ((((long)chunky) << 32) + chunkx);
	    //System.out.println("request chunk x:" + x + " y:" + y + " chunkx:" + chunkx + " chunky:" + chunky + " location:" + location);
	    if(_chunks.containsKey(location))
		return _chunks.get(location);
	    else
	    {
		Chunk c = new Chunk(x,y);
		_chunks.put(location, c);
		return c;
	    }
	}
    }
    public Chunk getChunkByChunkLoc(int x,int y){
	return getChunk(x << 6,y << 6);
    }
    public void registerPlayer(Player player){
	synchronized(_players){
	    if(!_players.containsKey(player.getId())){
		_players.put(player.getId(),player);
		System.out.println("Registering player " + player.getUsername());
	    }

	}
    }
    public void forgetPlayer(Player player){
	synchronized(_chunks){
	    for(Object c : _chunks.getValues())
		((Chunk)c).forget(player);
	}
	synchronized(_players){
	    if(_players.containsKey(player.getId()))
		_players.remove(player.getId());
	}
    }
    public Collection<Player> getPlayers(){
	synchronized(_players)
	{
	    return _players.values();
	}
    }
    public Block getBlock(int x,int y, int z){
	return getChunk(x,y).getBlock(x, y, z);
    }

    public static World getInstance() {
	return WorldHolder.INSTANCE;
    }

    public Chunk getChunk(Player player) {
	return getChunk((int)player.getX(),(int)player.getY());
    }

    public void unloadChunk(int _x, int _y) {
	long location = ((((long)_y) << 32) + _x);
	synchronized(_chunks){
	    _chunks.remove(location);
	}
    }
    private static class WorldHolder {
	private static final World INSTANCE = new World();
    }
}
