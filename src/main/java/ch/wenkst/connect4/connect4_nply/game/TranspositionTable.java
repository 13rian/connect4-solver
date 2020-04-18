package ch.wenkst.connect4.connect4_nply.game;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class TranspositionTable {
	private Int2ObjectOpenHashMap<Entry> entries; 	// all entries in the table, the modulo key is the index if the array
	private int size; 										// total number of possible entries
	


	/**
	 * defines a transposition table with fixed size, modulo keys are used. if a collision occurs the latest entry is kept
	 * more that one positions could have the same modulo key therefore the unique key of the position is kept as well
	 * @param size 		number of entries in the transposition table
	 */
	public TranspositionTable(int size) {
		this.size = size;
		entries = new Int2ObjectOpenHashMap<>(AppConfig.tpTableSize);
	}
	
	
	public void clear() {
		entries.clear();
	}


	/**
	 * returns the modulo key of the passed position key
	 * @param positionKey	unique key of the position
	 * @return				key in the transposition table
	 */
	private int getModuloKey(long positionKey) {
		return (int) (positionKey % this.size);
	}

	
	/**
	 * adds a position to the transposition table, in case of a collision the new value is kept
	 * @param positionKey: 		unique key of the position
	 * @param score: 			the score of the position
	 */
	public void put(long positionKey, byte score) {
		int i = getModuloKey(positionKey);
		Entry entry = new Entry(positionKey, score);
		entries.put(i, entry);                    
	}


	/** 
	 * returns the value of a position for the passed position key
	 * @param positionKey 	unique key of the position		
	 * @return 				the score of the position or 0 if the key is not found
	 */
	public byte get(long positionKey) {
		int i = getModuloKey(positionKey); 
		Entry entry = entries.get(i);
		if (entry != null && entry.key == positionKey) {   // avoid error due to collisions
			return entry.score;
		} else { 
			return 0;            
		}
	}


	/**
	 * define one entry in the transposition table, the size is 72 bits
	 */
	private class Entry {
		private Entry(long key, byte score) {
			this.key = key;
			this.score = score;
		}
		
	    @Override
	    public int hashCode() {
	    	return getModuloKey(key);
	    }
		
		long key; 		 // the key of the position 	64 bits
		byte score;      // the score of the position 	8 bits
	}

}